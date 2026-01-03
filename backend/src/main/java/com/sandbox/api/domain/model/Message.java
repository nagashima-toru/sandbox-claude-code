package com.sandbox.api.domain.model;

public class Message {
    private Long id;
    private String code;
    private String content;

    public Message() {
    }

    public Message(Long id, String code, String content) {
        this.id = id;
        this.code = code;
        this.content = content;
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
