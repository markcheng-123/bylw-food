package com.bylw.foodforum.dto.ai;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AiChatDTO {

    @NotBlank(message = "message is required")
    @Size(max = 2000, message = "message is too long")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
