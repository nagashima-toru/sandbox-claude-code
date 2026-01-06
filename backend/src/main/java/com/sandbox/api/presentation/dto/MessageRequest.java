package com.sandbox.api.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MessageRequest {

    @NotBlank(message = "Code is required")
    @Size(min = 1, max = 50, message = "Code must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$",
            message = "Code must contain only alphanumeric characters, hyphens, and underscores")
    private String code;

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 255, message = "Content must be between 1 and 255 characters")
    private String content;

    public MessageRequest() {
    }

    public MessageRequest(String code, String content) {
        this.code = code;
        this.content = content;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
