package com.sandbox.api.presentation.dto;

import com.sandbox.api.domain.model.Message;

public class MessageResponse {

    private Long id;
    private String code;
    private String content;

    public MessageResponse() {
    }

    public MessageResponse(Long id, String code, String content) {
        this.id = id;
        this.code = code;
        this.content = content;
    }

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getCode(),
                message.getContent()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
