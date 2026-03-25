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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class AiServiceImpl implements AiService {

    private static final Pattern BUDGET_PATTERN = Pattern.compile("(\\d{1,4})\\s*(元|块|rmb|RMB|¥|￥)?");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FoodPostMapper foodPostMapper;
    private final FoodCategoryService foodCategoryService;

    @Value("${app.ai.enabled:false}")
    private boolean enabled;

    @Value("${app.ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${app.ai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.system-prompt:你是本地美食论坛的AI虚拟推荐官，请用简短友好的中文回答。}")
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

        // 后续可以通过修改 base-url/model 切换到 DeepSeek 等模型。
        if (enabled && StringUtils.hasText(apiKey)) {
            try {
                return callModel(userMessage, context);
            } catch (Exception ex) {
                // 模型不可用时自动降级为本地数据推荐。
                return buildLocalReply(userMessage, budget, candidates, "模型暂不可用，已切换到本地美食推荐：");
            }
        }

        return buildLocalReply(userMessage, budget, candidates, "当前为本地数据推荐（未启用外部大模型）：");
    }

    private String callModel(String userMessage, String context) throws Exception {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("model", model);
        body.put("temperature", 0.4);
        body.put("messages", Arrays.asList(
            buildMessage("system", systemPrompt),
            buildMessage("system", "以下是平台内可用的美食帖子数据，请优先基于这些数据推荐：\n" + context),
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
                .like(FoodPost::getAddress, keyword));
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
            return "暂无可用帖子数据";
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
                "#%d | %s | 分类:%s | 人均:%s | 地址:%s | 简介:%s",
                post.getId(),
                safe(post.getTitle()),
                safe(categoryMap.get(post.getCategoryId())),
                post.getPerCapita() == null ? "未知" : post.getPerCapita() + "元",
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
            sb.append("暂时没找到匹配内容，你可以换个关键词或预算再试试。");
            return sb.toString();
        }

        if (budget != null) {
            sb.append("按人均 ").append(budget).append(" 元以内为你筛选如下：\n");
        } else {
            sb.append("按热度为你推荐如下：\n");
        }

        for (int i = 0; i < posts.size() && i < 5; i++) {
            FoodPost post = posts.get(i);
            sb.append(i + 1)
                .append(". ")
                .append(safe(post.getTitle()))
                .append("（人均：")
                .append(post.getPerCapita() == null ? "未知" : post.getPerCapita() + "元")
                .append("，地址：")
                .append(safe(post.getAddress()))
                .append("，详情：/food/")
                .append(post.getId())
                .append("）\n");
        }

        sb.append("\n你也可以继续补充口味、区域、预算，我再细化推荐。"
        );
        sb.append("\n本次问题：").append(userMessage);
        return sb.toString().trim();
    }

    private Map<String, String> buildMessage(String role, String content) {
        Map<String, String> message = new HashMap<String, String>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value.trim() : "未知";
    }
}
