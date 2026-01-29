package com.sandbox.api.presentation.controller;

import com.sandbox.api.application.usecase.CreateMessageUseCase;
import com.sandbox.api.application.usecase.DeleteMessageUseCase;
import com.sandbox.api.application.usecase.GetAllMessagesUseCase;
import com.sandbox.api.application.usecase.GetMessageByIdUseCase;
import com.sandbox.api.application.usecase.UpdateMessageUseCase;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.presentation.dto.MessageRequest;
import com.sandbox.api.presentation.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** REST controller for message management endpoints. */
@RestController
@RequestMapping("/api")
@Tag(name = "Message", description = "メッセージ管理API")
public class MessageController {

  private final GetAllMessagesUseCase getAllMessagesUseCase;
  private final GetMessageByIdUseCase getMessageByIdUseCase;
  private final CreateMessageUseCase createMessageUseCase;
  private final UpdateMessageUseCase updateMessageUseCase;
  private final DeleteMessageUseCase deleteMessageUseCase;

  /**
   * Constructs a new MessageController with the required use cases.
   *
   * @param getAllMessagesUseCase use case for retrieving all messages
   * @param getMessageByIdUseCase use case for retrieving a message by ID
   * @param createMessageUseCase use case for creating a message
   * @param updateMessageUseCase use case for updating a message
   * @param deleteMessageUseCase use case for deleting a message
   */
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

  /**
   * Retrieves all messages.
   *
   * @return response entity containing a list of all messages
   */
  @Operation(summary = "全メッセージ取得", description = "すべてのメッセージを取得します")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "成功")})
  @GetMapping(value = "/messages", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<MessageResponse>> getAllMessages() {
    List<Message> messages = getAllMessagesUseCase.execute();
    List<MessageResponse> responses = messages.stream().map(MessageResponse::from).toList();
    return ResponseEntity.ok(responses);
  }

  /**
   * Retrieves a message by its ID.
   *
   * @param id the ID of the message to retrieve
   * @return response entity containing the message
   */
  @Operation(summary = "メッセージ取得", description = "IDでメッセージを取得します")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "404", description = "メッセージが見つかりません")
      })
  @GetMapping(value = "/messages/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long id) {
    Message message = getMessageByIdUseCase.execute(id);
    return ResponseEntity.ok(MessageResponse.from(message));
  }

  /**
   * Creates a new message.
   *
   * @param request the message request containing code and content
   * @param ucb URI components builder for creating location header
   * @return response entity containing the created message with location header
   */
  @Operation(summary = "メッセージ作成", description = "新しいメッセージを作成します")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "作成成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
        @ApiResponse(responseCode = "409", description = "コードが既に存在します")
      })
  @PostMapping(
      value = "/messages",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MessageResponse> createMessage(
      @Valid @RequestBody MessageRequest request, UriComponentsBuilder ucb) {
    Message message = createMessageUseCase.execute(request.getCode(), request.getContent());
    URI location = ucb.path("/api/messages/{id}").buildAndExpand(message.getId()).toUri();
    return ResponseEntity.created(location).body(MessageResponse.from(message));
  }

  /**
   * Updates an existing message.
   *
   * @param id the ID of the message to update
   * @param request the message request containing updated code and content
   * @return response entity containing the updated message
   */
  @Operation(summary = "メッセージ更新", description = "既存のメッセージを更新します")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
        @ApiResponse(responseCode = "404", description = "メッセージが見つかりません"),
        @ApiResponse(responseCode = "409", description = "コードが既に存在します")
      })
  @PutMapping(
      value = "/messages/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MessageResponse> updateMessage(
      @PathVariable Long id, @Valid @RequestBody MessageRequest request) {
    Message message = updateMessageUseCase.execute(id, request.getCode(), request.getContent());
    return ResponseEntity.ok(MessageResponse.from(message));
  }

  /**
   * Deletes a message by its ID.
   *
   * @param id the ID of the message to delete
   * @return response entity with no content
   */
  @Operation(summary = "メッセージ削除", description = "メッセージを削除します")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "削除成功"),
        @ApiResponse(responseCode = "404", description = "メッセージが見つかりません")
      })
  @DeleteMapping("/messages/{id}")
  public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
    deleteMessageUseCase.execute(id);
    return ResponseEntity.noContent().build();
  }
}
