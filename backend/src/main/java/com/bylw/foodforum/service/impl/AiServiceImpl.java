package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.entity.FoodCategory;
import com.bylw.foodforum.entity.FoodPost;
import com.bylw.foodforum.mapper.FoodPostMapper;
import com.bylw.foodforum.service.AiService;
import com.bylw.foodforum.service.FoodCategoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AiServiceImpl implements AiService {

    private static final Pattern BUDGET_PATTERN = Pattern.compile("(\\d{1,4})");
    private static final long SSE_TIMEOUT_MS = 1000L * 60L * 5L;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FoodPostMapper foodPostMapper;
    private final FoodCategoryService foodCategoryService;

    @Value("${app.ai.enabled:false}")
    private boolean enabled;

    @Value("${app.ai.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${app.ai.model:deepseek-chat}")
    private String model;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.system-prompt:You are an AI food recommendation assistant. Prioritize recommendations based on real posts from this platform.}")
    private String systemPrompt;

    public AiServiceImpl(
        RestTemplateBuilder builder,
        ObjectMapper objectMapper,
        FoodPostMapper foodPostMapper,
        FoodCategoryService foodCategoryService
    ) {
        this.restTemplate = builder.build();
        this.objectMapper = objectMapper;
        this.foodPostMapper = foodPostMapper;
        this.foodCategoryService = foodCategoryService;
    }

    @Override
    public String chat(String message) {
        if (!StringUtils.hasText(message)) {
            throw new BusinessException("Message cannot be empty");
        }

        String userMessage = message.trim();
        Integer budget = extractBudget(userMessage);
        List<FoodPost> candidates = queryCandidatePosts(userMessage, budget);
        String context = buildPostContext(candidates);

        if (enabled && StringUtils.hasText(apiKey)) {
            try {
                return callModel(userMessage, context);
            } catch (Exception ex) {
                return buildLocalReply(userMessage, budget, candidates, "AI is temporarily unavailable. Switched to local recommendation mode:");
            }
        }
        return buildLocalReply(userMessage, budget, candidates, "Running in local recommendation mode:");
    }

    @Override
    public SseEmitter streamChat(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new BusinessException("Prompt cannot be empty");
        }

        final String userPrompt = prompt.trim();
        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        final AtomicBoolean closed = new AtomicBoolean(false);

        emitter.onCompletion(() -> closed.set(true));
        emitter.onTimeout(() -> {
            closed.set(true);
            emitter.complete();
        });
        emitter.onError(ex -> closed.set(true));

        CompletableFuture.runAsync(() -> {
            try {
                streamChatInternal(userPrompt, emitter, closed);
            } catch (Exception ex) {
                safeSend(emitter, closed, "error", "stream_failed:" + ex.getMessage());
                safeComplete(emitter, closed);
            }
        });

        return emitter;
    }

    private void streamChatInternal(String userPrompt, SseEmitter emitter, AtomicBoolean closed) {
        Integer budget = extractBudget(userPrompt);
        List<FoodPost> candidates = queryCandidatePosts(userPrompt, budget);
        String context = buildPostContext(candidates);

        if (!(enabled && StringUtils.hasText(apiKey))) {
            streamLocalReply(userPrompt, budget, candidates, emitter, closed);
            return;
        }

        try {
            streamFromModel(userPrompt, context, emitter, closed);
            safeSend(emitter, closed, "done", "[DONE]");
            safeComplete(emitter, closed);
        } catch (Exception ex) {
            safeSend(emitter, closed, "error", "model_unavailable_fallback_local");
            streamLocalReply(userPrompt, budget, candidates, emitter, closed);
        }
    }

    private void streamFromModel(String userPrompt, String context, SseEmitter emitter, AtomicBoolean closed) {
        final String url = baseUrl + "/chat/completions";

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("model", model);
        body.put("temperature", 0.35);
        body.put("stream", Boolean.TRUE);
        body.put("messages", Arrays.asList(
            buildMessage("system", systemPrompt),
            buildMessage("system", "Real post data from this platform:\n" + context),
            buildMessage("user", userPrompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        restTemplate.execute(
            url,
            HttpMethod.POST,
            request -> {
                request.getHeaders().putAll(headers);
                objectMapper.writeValue(request.getBody(), body);
            },
            response -> {
                InputStream responseBody = response.getBody();
                if (responseBody == null) {
                    throw new BusinessException("AI stream response is empty");
                }

                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseBody, StandardCharsets.UTF_8))) {
                    String line;
                    while (!closed.get() && (line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!StringUtils.hasText(line) || !line.startsWith("data:")) {
                            continue;
                        }
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        String token = parseStreamToken(data);
                        if (StringUtils.hasText(token)) {
                            safeSend(emitter, closed, "data", token);
                        }
                    }
                }
                return null;
            }
        );
    }

    private String parseStreamToken(String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.size() == 0) {
                return "";
            }

            JsonNode first = choices.path(0);
            JsonNode deltaContent = first.path("delta").path("content");
            if (deltaContent.isTextual()) {
                return deltaContent.asText();
            }
            JsonNode messageContent = first.path("message").path("content");
            if (messageContent.isTextual()) {
                return messageContent.asText();
            }
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    private void streamLocalReply(
        String userPrompt,
        Integer budget,
        List<FoodPost> candidates,
        SseEmitter emitter,
        AtomicBoolean closed
    ) {
        String reply = buildLocalReply(userPrompt, budget, candidates, "Running in local recommendation mode:");
        for (int i = 0; i < reply.length() && !closed.get(); i += 3) {
            int end = Math.min(i + 3, reply.length());
            safeSend(emitter, closed, "data", reply.substring(i, end));
            try {
                Thread.sleep(14L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        safeSend(emitter, closed, "done", "[DONE]");
        safeComplete(emitter, closed);
    }

    private void safeSend(SseEmitter emitter, AtomicBoolean closed, String event, String data) {
        if (closed.get()) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException | IllegalStateException ex) {
            closed.set(true);
        }
    }

    private void safeComplete(SseEmitter emitter, AtomicBoolean closed) {
        if (closed.get()) {
            return;
        }
        closed.set(true);
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // ignore
        }
    }

    private String callModel(String userMessage, String context) throws Exception {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("model", model);
        body.put("temperature", 0.35);
        body.put("messages", Arrays.asList(
            buildMessage("system", systemPrompt),
            buildMessage("system", "Real post data from this platform:\n" + context),
            buildMessage("user", userMessage)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/chat/completions", entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (!contentNode.isTextual()) {
            throw new BusinessException("AI response is empty");
        }
        return contentNode.asText();
    }

    private Integer extractBudget(String message) {
        Matcher matcher = BUDGET_PATTERN.matcher(message);
        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            if (value > 0 && value <= 5000) {
                return value;
            }
        }
        return null;
    }

    private List<FoodPost> queryCandidatePosts(String message, Integer budget) {
        LambdaQueryWrapper<FoodPost> wrapper = new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getStatus, 1)
            .orderByDesc(FoodPost::getLikeCount)
            .orderByDesc(FoodPost::getCommentCount)
            .orderByDesc(FoodPost::getViewCount)
            .orderByDesc(FoodPost::getCreatedAt)
            .last("LIMIT 20");

        if (budget != null) {
            wrapper.isNotNull(FoodPost::getPerCapita).le(FoodPost::getPerCapita, budget);
        }

        if (StringUtils.hasText(message)) {
            String keyword = message.trim();
            wrapper.and(item -> item.like(FoodPost::getTitle, keyword)
                .or()
                .like(FoodPost::getSummary, keyword)
                .or()
                .like(FoodPost::getAddress, keyword)
                .or()
                .like(FoodPost::getContent, keyword));
        }

        List<FoodPost> posts = foodPostMapper.selectList(wrapper);
        if (!posts.isEmpty()) {
            return posts.stream().limit(8).collect(Collectors.toList());
        }

        LambdaQueryWrapper<FoodPost> fallback = new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getStatus, 1)
            .orderByDesc(FoodPost::getLikeCount)
            .orderByDesc(FoodPost::getCommentCount)
            .orderByDesc(FoodPost::getViewCount)
            .orderByDesc(FoodPost::getCreatedAt)
            .last("LIMIT 8");

        if (budget != null) {
            fallback.isNotNull(FoodPost::getPerCapita).le(FoodPost::getPerCapita, budget);
        }

        return foodPostMapper.selectList(fallback);
    }

    private String buildPostContext(List<FoodPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return "No available post data";
        }

        List<Long> categoryIds = posts.stream()
            .map(FoodPost::getCategoryId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, String> categoryMap = categoryIds.isEmpty()
            ? new HashMap<Long, String>()
            : foodCategoryService.listByIds(categoryIds)
                .stream()
                .collect(Collectors.toMap(FoodCategory::getId, FoodCategory::getName, (left, right) -> left, HashMap::new));

        List<String> lines = new ArrayList<String>();
        for (FoodPost post : posts) {
            String line = String.format(
                "#%d | %s | category:%s | per_capita:%s | address:%s | summary:%s",
                post.getId(),
                safe(post.getTitle()),
                safe(categoryMap.get(post.getCategoryId())),
                post.getPerCapita() == null ? "unknown" : post.getPerCapita() + " CNY",
                safe(post.getAddress()),
                safe(post.getSummary())
            );
            lines.add(line);
        }
        return String.join("\n", lines);
    }

    private String buildLocalReply(String userMessage, Integer budget, List<FoodPost> posts, String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("\n");

        if (posts == null || posts.isEmpty()) {
            sb.append("No matching content found. Try another keyword, area, or budget.");
            return sb.toString();
        }

        if (budget != null) {
            sb.append("Recommendations under ").append(budget).append(" CNY per person:\n");
        } else {
            sb.append("Recommendations by current popularity:\n");
        }

        for (int i = 0; i < posts.size() && i < 5; i++) {
            FoodPost post = posts.get(i);
            sb.append(i + 1)
                .append(". ")
                .append(safe(post.getTitle()))
                .append(" (per capita: ")
                .append(post.getPerCapita() == null ? "unknown" : post.getPerCapita() + " CNY")
                .append(", address: ")
                .append(safe(post.getAddress()))
                .append(", detail: /food/")
                .append(post.getId())
                .append(")\n");
        }

        sb.append("\nShare more taste, budget, and location details for a more precise recommendation.");
        sb.append("\nInput: ").append(userMessage);
        return sb.toString().trim();
    }

    private Map<String, String> buildMessage(String role, String content) {
        Map<String, String> message = new HashMap<String, String>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value.trim() : "unknown";
    }
}
