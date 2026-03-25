package com.bylw.foodforum.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiService {

    String chat(String message);

    SseEmitter streamChat(String prompt);
}
