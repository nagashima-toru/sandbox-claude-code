package com.sandbox.api.presentation.controller;

import com.sandbox.api.application.usecase.GetMessageUseCase;
import com.sandbox.api.domain.model.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Message", description = "メッセージ管理API")
public class MessageController {

    private final GetMessageUseCase getMessageUseCase;

    public MessageController(GetMessageUseCase getMessageUseCase) {
        this.getMessageUseCase = getMessageUseCase;
    }

    @Operation(summary = "メッセージ取得", description = "データベースからメッセージを取得します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "メッセージ取得成功")
    })
    @GetMapping("/message")
    public String getMessage() {
        return getMessageUseCase.execute("hello")
                .map(Message::getContent)
                .orElse("Message not found");
    }
}
