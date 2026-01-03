package com.sandbox.api.presentation.controller;

import com.sandbox.api.application.usecase.GetMessageUseCase;
import com.sandbox.api.domain.model.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final GetMessageUseCase getMessageUseCase;

    public MessageController(GetMessageUseCase getMessageUseCase) {
        this.getMessageUseCase = getMessageUseCase;
    }

    @GetMapping("/message")
    public String getMessage() {
        return getMessageUseCase.execute("hello")
                .map(Message::getContent)
                .orElse("Message not found");
    }
}
