package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("project", "local-food-forum");
        data.put("status", "UP");
        data.put("stage", "bootstrap");
        return ApiResponse.success(data);
    }
}
