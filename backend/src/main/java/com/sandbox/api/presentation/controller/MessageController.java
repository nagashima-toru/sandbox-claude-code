package com.sandbox.api.presentation.controller;

import com.sandbox.api.application.usecase.*;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.presentation.dto.MessageRequest;
import com.sandbox.api.presentation.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Message", description = "メッセージ管理API")
public class MessageController {

    private final GetAllMessagesUseCase getAllMessagesUseCase;
    private final GetMessageByIdUseCase getMessageByIdUseCase;
    private final CreateMessageUseCase createMessageUseCase;
    private final UpdateMessageUseCase updateMessageUseCase;
    private final DeleteMessageUseCase deleteMessageUseCase;

    public MessageController(
            GetAllMessagesUseCase getAllMessagesUseCase,
            GetMessageByIdUseCase getMessageByIdUseCase,
            CreateMessageUseCase createMessageUseCase,
            UpdateMessageUseCase updateMessageUseCase,
            DeleteMessageUseCase deleteMessageUseCase) {
        this.getAllMessagesUseCase = getAllMessagesUseCase;
        this.getMessageByIdUseCase = getMessageByIdUseCase;
        this.createMessageUseCase = createMessageUseCase;
        this.updateMessageUseCase = updateMessageUseCase;
        this.deleteMessageUseCase = deleteMessageUseCase;
    }

    @Operation(summary = "全メッセージ取得", description = "すべてのメッセージを取得します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功")
    })
    @GetMapping("/messages")
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        List<Message> messages = getAllMessagesUseCase.execute();
        List<MessageResponse> responses = messages.stream()
                .map(MessageResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "メッセージ取得", description = "IDでメッセージを取得します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "404", description = "メッセージが見つかりません")
    })
    @GetMapping("/messages/{id}")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long id) {
        Message message = getMessageByIdUseCase.execute(id);
        return ResponseEntity.ok(MessageResponse.from(message));
    }

    @Operation(summary = "メッセージ作成", description = "新しいメッセージを作成します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "作成成功"),
            @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
            @ApiResponse(responseCode = "409", description = "コードが既に存在します")
    })
    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> createMessage(
            @Valid @RequestBody MessageRequest request,
            UriComponentsBuilder ucb) {
        Message message = createMessageUseCase.execute(
                request.getCode(),
                request.getContent()
        );
        URI location = ucb.path("/api/messages/{id}")
                .buildAndExpand(message.getId())
                .toUri();
        return ResponseEntity.created(location).body(MessageResponse.from(message));
    }

    @Operation(summary = "メッセージ更新", description = "既存のメッセージを更新します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
            @ApiResponse(responseCode = "404", description = "メッセージが見つかりません"),
            @ApiResponse(responseCode = "409", description = "コードが既に存在します")
    })
    @PutMapping("/messages/{id}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable Long id,
            @Valid @RequestBody MessageRequest request) {
        Message message = updateMessageUseCase.execute(
                id,
                request.getCode(),
                request.getContent()
        );
        return ResponseEntity.ok(MessageResponse.from(message));
    }

    @Operation(summary = "メッセージ削除", description = "メッセージを削除します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "削除成功"),
            @ApiResponse(responseCode = "404", description = "メッセージが見つかりません")
    })
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        deleteMessageUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
