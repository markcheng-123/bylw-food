package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.dto.ai.AiChatDTO;
import com.bylw.foodforum.service.AiService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ApiResponse<Map<String, String>> chat(@Valid @RequestBody AiChatDTO chatDTO) {
        String reply = aiService.chat(chatDTO.getMessage());
        Map<String, String> data = new HashMap<String, String>();
        data.put("reply", reply);
        return ApiResponse.success(data);
    }
}
