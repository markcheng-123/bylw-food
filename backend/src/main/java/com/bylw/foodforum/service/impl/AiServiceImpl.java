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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final Pattern LOCATION_HINT_PATTERN = Pattern.compile("(?:在|到|去|位于|附近)\\s*([\\p{L}\\p{N}\\-·\\s]{2,30})");
    private static final Pattern PUNCT_PATTERN = Pattern.compile("[,，。！？!?.；;、/\\\\]+");
    private static final long SSE_TIMEOUT_MS = 1000L * 60L * 5L;

    private static final Set<String> NOISE_WORDS = new HashSet<String>(Arrays.asList(
        "推荐", "推荐下", "推荐一下", "有推荐吗", "有没有", "想吃", "吃什么", "哪里", "附近",
        "我想", "我在", "请问", "帮我", "给我", "来点", "一下", "没事", "吗", "呢", "吧", "呀",
        "预算", "人均", "元", "cny", "rmb"
    ));

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

    @Value("${app.ai.system-prompt:你是智能美食导航专家。请基于站内真实数据输出。}")
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

        String userMessage = canonicalizeUserQuery(message.trim());
        Integer budget = extractBudget(userMessage);
        RecommendationPlan plan = prepareRecommendationPlan(userMessage, budget);
        return buildLocalReply(budget, plan);
    }

    @Override
    public SseEmitter streamChat(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new BusinessException("Prompt cannot be empty");
        }

        final String userPrompt = canonicalizeUserQuery(prompt.trim());
        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        final AtomicBoolean closed = new AtomicBoolean(false);

        emitter.onCompletion(new Runnable() {
            @Override
            public void run() {
                closed.set(true);
            }
        });
        emitter.onTimeout(new Runnable() {
            @Override
            public void run() {
                closed.set(true);
                emitter.complete();
            }
        });
        emitter.onError(ex -> closed.set(true));

        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    streamChatInternal(userPrompt, emitter, closed);
                } catch (Exception ex) {
                    safeSend(emitter, closed, "error", "stream_failed:" + ex.getMessage());
                    safeComplete(emitter, closed);
                }
            }
        });

        return emitter;
    }

    private void streamChatInternal(String userPrompt, SseEmitter emitter, AtomicBoolean closed) {
        Integer budget = extractBudget(userPrompt);
        RecommendationPlan plan = prepareRecommendationPlan(userPrompt, budget);
        streamLocalReply(budget, plan, emitter, closed);
    }

    private void streamFromModel(String userPrompt, String context, SseEmitter emitter, AtomicBoolean closed) {
        final String url = baseUrl + "/chat/completions";

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("model", model);
        body.put("temperature", 0.2);
        body.put("stream", Boolean.TRUE);
        body.put("messages", Arrays.asList(
            buildMessage("system", systemPrompt),
            buildMessage("system", "平台真实帖子数据:\n" + context),
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
        Integer budget,
        RecommendationPlan plan,
        SseEmitter emitter,
        AtomicBoolean closed
    ) {
        String reply = buildLocalReply(budget, plan);
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
        body.put("temperature", 0.2);
        body.put("messages", Arrays.asList(
            buildMessage("system", systemPrompt),
            buildMessage("system", "平台真实帖子数据:\n" + context),
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

    private RecommendationPlan prepareRecommendationPlan(String message, Integer budget) {
        String targetLocation = extractLocationKeyword(message);
        List<FoodPost> locationPosts = queryPostsByLocation(targetLocation, budget, 8);
        boolean locationMatched = !locationPosts.isEmpty();
        List<FoodPost> candidates = locationMatched ? locationPosts : queryCandidatePosts(message, budget);
        return new RecommendationPlan(targetLocation, locationMatched, candidates);
    }

    private List<FoodPost> queryPostsByLocation(String location, Integer budget, int limit) {
        if (!StringUtils.hasText(location)) {
            return new ArrayList<FoodPost>();
        }

        List<String> aliases = buildLocationAliases(location);
        if (aliases.isEmpty()) {
            return new ArrayList<FoodPost>();
        }

        LambdaQueryWrapper<FoodPost> wrapper = new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getStatus, 1)
            .orderByDesc(FoodPost::getLikeCount)
            .orderByDesc(FoodPost::getCommentCount)
            .orderByDesc(FoodPost::getViewCount)
            .orderByDesc(FoodPost::getCreatedAt)
            .last("LIMIT " + Math.max(1, limit));

        if (budget != null) {
            wrapper.isNotNull(FoodPost::getPerCapita).le(FoodPost::getPerCapita, budget);
        }

        wrapper.and(item -> {
            for (int i = 0; i < aliases.size(); i++) {
                if (i > 0) {
                    item.or();
                }
                item.like(FoodPost::getAddress, aliases.get(i));
            }
        });

        return foodPostMapper.selectList(wrapper);
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

        List<String> keywords = extractSearchKeywords(message);
        if (!keywords.isEmpty()) {
            wrapper.and(item -> {
                boolean started = false;
                for (String keyword : keywords) {
                    if (!StringUtils.hasText(keyword)) {
                        continue;
                    }
                    if (started) {
                        item.or();
                    }
                    item.like(FoodPost::getTitle, keyword)
                        .or()
                        .like(FoodPost::getSummary, keyword)
                        .or()
                        .like(FoodPost::getAddress, keyword)
                        .or()
                        .like(FoodPost::getContent, keyword);
                    started = true;
                }
            });
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

    private String buildLocalReply(Integer budget, RecommendationPlan plan) {
        List<FoodPost> posts = plan.candidates;
        String targetLocation = StringUtils.hasText(plan.targetLocation) ? plan.targetLocation : inferTargetLocationFromPosts(posts);

        StringBuilder sb = new StringBuilder();
        sb.append("📍 目标位置： ").append(targetLocation).append("\n");
        sb.append("🍽 推荐方案：").append("\n");

        if (StringUtils.hasText(plan.targetLocation) && !plan.locationMatched) {
            sb.append("- 未找到“").append(plan.targetLocation).append("”的站内地址数据。\n");
            FoodPost backup = findBackupPost(budget);
            if (backup != null) {
                sb.append("- **").append(safe(backup.getTitle())).append("** - ")
                    .append(pickFeature(backup))
                    .append(" | ")
                    .append(formatPerCapita(backup.getPerCapita()))
                    .append("\n");
            } else {
                sb.append("- **暂无备选** - 请补充预算或附近商圈关键词\n");
            }
            sb.append("💡 建议：先使用备选区域筛选，再补充预算可提升命中率。");
            return sb.toString();
        }

        if (posts == null || posts.isEmpty()) {
            sb.append("- 未找到可用推荐数据。\n");
            sb.append("💡 建议：补充具体区域和预算，例如“武昌 人均50”。");
            return sb.toString();
        }

        for (int i = 0; i < posts.size() && i < 3; i++) {
            FoodPost post = posts.get(i);
            sb.append("- **").append(safe(post.getTitle())).append("** - ")
                .append(pickFeature(post))
                .append(" | ")
                .append(formatPerCapita(post.getPerCapita()))
                .append("\n");
        }

        if (budget != null) {
            sb.append("💡 建议：建议优先筛选人均 ").append(budget).append(" 元以内，并避开 12:00 高峰期。");
        } else {
            sb.append("💡 建议：建议避开 12:00 高峰期，优先选择可提前下单门店。");
        }
        return sb.toString();
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

    private String canonicalizeUserQuery(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        return input.trim();
    }

    private String normalizeSearchKeyword(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String text = raw.trim().toLowerCase();
        text = text.replaceAll("(我想吃|我想|我在|想吃|想找|想要|来点|请问|有没有|推荐一下|推荐|附近|哪里|吃什么|没事)", " ");
        text = text.replaceAll("(人均\\s*\\d{1,4}|预算\\s*\\d{1,4}|\\d{1,4}\\s*元)", " ");
        text = PUNCT_PATTERN.matcher(text).replaceAll(" ");
        text = text.replaceAll("[^\\p{L}\\p{N}]+", " ");
        text = text.replaceAll("\\s+", " ").trim();
        return StringUtils.hasText(text) ? text : raw.trim();
    }

    private List<String> extractSearchKeywords(String message) {
        Set<String> set = new LinkedHashSet<String>();
        String raw = message == null ? "" : message.trim();
        if (StringUtils.hasText(raw)) {
            set.add(raw);
        }

        String normalized = normalizeSearchKeyword(raw);
        if (StringUtils.hasText(normalized)) {
            set.add(normalized);

            String compact = normalized.replace(" ", "");
            if (StringUtils.hasText(compact)) {
                set.add(compact);
            }

            for (String token : normalized.split("\\s+")) {
                if (StringUtils.hasText(token) && token.length() >= 2 && !NOISE_WORDS.contains(token)) {
                    set.add(token.trim());
                }
            }
        }

        List<String> result = new ArrayList<String>();
        for (String keyword : set) {
            if (StringUtils.hasText(keyword) && keyword.trim().length() >= 2) {
                result.add(keyword.trim());
            }
        }
        return result;
    }

    private String extractLocationKeyword(String message) {
        if (!StringUtils.hasText(message)) {
            return "";
        }

        String raw = message.trim();
        Matcher matcher = LOCATION_HINT_PATTERN.matcher(raw);
        while (matcher.find()) {
            String candidate = cleanLocationCandidate(matcher.group(1));
            if (isLikelyLocationToken(candidate)) {
                return candidate;
            }
        }

        String normalized = normalizeSearchKeyword(raw);
        if (!StringUtils.hasText(normalized)) {
            return "";
        }

        String best = "";
        for (String token : normalized.split("\\s+")) {
            String candidate = cleanLocationCandidate(token);
            if (!isLikelyLocationToken(candidate)) {
                continue;
            }
            if (candidate.length() > best.length()) {
                best = candidate;
            }
        }
        return best;
    }

    private String cleanLocationCandidate(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String text = raw.trim();
        text = text.replaceAll("(人均\\s*\\d{1,4}|预算\\s*\\d{1,4}|\\d{1,4}\\s*元)", " ");
        text = text.replaceAll("(推荐一下|推荐|想吃|吃什么|有没有|附近|哪里|没事|一下|请问|帮我)", " ");
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }

    private boolean isLikelyLocationToken(String token) {
        if (!StringUtils.hasText(token) || token.length() < 2) {
            return false;
        }
        String text = token.trim();
        if (NOISE_WORDS.contains(text) || text.matches("\\d+")) {
            return false;
        }
        String lower = text.toLowerCase();
        if (lower.contains("省") || lower.contains("市") || lower.contains("区") || lower.contains("县")
            || lower.contains("镇") || lower.contains("乡") || lower.contains("村")
            || lower.contains("路") || lower.contains("街") || lower.contains("道") || lower.contains("巷")
            || lower.contains("广场") || lower.contains("商圈") || lower.contains("中心")
            || lower.contains("defense") || lower.contains("défense")) {
            return true;
        }
        return text.matches(".*[\\u4e00-\\u9fa5].*") && text.length() <= 12;
    }

    private List<String> buildLocationAliases(String location) {
        LinkedHashSet<String> aliases = new LinkedHashSet<String>();
        if (!StringUtils.hasText(location)) {
            return new ArrayList<String>();
        }
        String loc = location.trim();
        aliases.add(loc);

        String compact = loc.replace(" ", "");
        if (!compact.equals(loc) && compact.length() >= 2) {
            aliases.add(compact);
        }

        String lower = loc.toLowerCase();
        if (loc.contains("拉德芳斯") || lower.contains("la défense") || lower.contains("la defense") || lower.contains("defense")) {
            aliases.add("拉德芳斯");
            aliases.add("La Défense");
            aliases.add("La Defense");
        }
        return new ArrayList<String>(aliases);
    }

    private FoodPost findBackupPost(Integer budget) {
        LambdaQueryWrapper<FoodPost> wrapper = new LambdaQueryWrapper<FoodPost>()
            .eq(FoodPost::getStatus, 1)
            .orderByDesc(FoodPost::getLikeCount)
            .orderByDesc(FoodPost::getCommentCount)
            .orderByDesc(FoodPost::getViewCount)
            .orderByDesc(FoodPost::getCreatedAt)
            .last("LIMIT 1");
        if (budget != null) {
            wrapper.isNotNull(FoodPost::getPerCapita).le(FoodPost::getPerCapita, budget);
        }
        List<FoodPost> posts = foodPostMapper.selectList(wrapper);
        return posts.isEmpty() ? null : posts.get(0);
    }

    private String inferTargetLocationFromPosts(List<FoodPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return "未识别具体位置";
        }
        FoodPost first = posts.get(0);
        return StringUtils.hasText(first.getAddress()) ? first.getAddress().trim() : "热门区域";
    }

    private String pickFeature(FoodPost post) {
        String summary = post == null ? "" : post.getSummary();
        if (StringUtils.hasText(summary)) {
            String s = summary.trim();
            return s.length() > 24 ? s.substring(0, 24) + "..." : s;
        }
        String title = safe(post == null ? null : post.getTitle());
        return title.length() > 24 ? title.substring(0, 24) + "..." : title;
    }

    private String formatPerCapita(Integer perCapita) {
        return perCapita == null ? "人均 未知" : ("人均 " + perCapita + " 元");
    }

    private static class RecommendationPlan {
        private final String targetLocation;
        private final boolean locationMatched;
        private final List<FoodPost> candidates;

        private RecommendationPlan(String targetLocation, boolean locationMatched, List<FoodPost> candidates) {
            this.targetLocation = targetLocation;
            this.locationMatched = locationMatched;
            this.candidates = candidates;
        }
    }
}
