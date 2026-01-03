package com.sandbox.api.controller;

import com.sandbox.api.entity.Message;
import com.sandbox.api.mapper.MessageMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageMapper messageMapper;

    public MessageController(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @GetMapping("/message")
    public String getMessage() {
        Message message = messageMapper.findByCode("hello");
        return message != null ? message.getContent() : "Message not found";
    }
}