package com.sandbox.api.presentation.controller;

import com.sandbox.api.application.usecase.CreateMessageUseCase;
import com.sandbox.api.application.usecase.DeleteMessageUseCase;
import com.sandbox.api.application.usecase.GetAllMessagesUseCase;
import com.sandbox.api.application.usecase.GetMessageByIdUseCase;
import com.sandbox.api.application.usecase.UpdateMessageUseCase;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.presentation.dto.MessageMapper;
import com.sandbox.api.presentation.dto.MessageRequest;
import com.sandbox.api.presentation.dto.MessageResponse;
import com.sandbox.api.presentation.generated.api.MessageApi;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** REST controller implementing the OpenAPI generated MessageApi interface. */
@RestController
public class MessageController implements MessageApi {

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

  @Override
  public ResponseEntity<com.sandbox.api.presentation.generated.model.MessagePage> getAllMessages(
      Integer page, Integer size) {
    Page<Message> messagePage = getAllMessagesUseCase.execute(page, size);
    return ResponseEntity.ok(MessageMapper.toMessagePage(messagePage));
  }

  @Override
  public ResponseEntity<com.sandbox.api.presentation.generated.model.MessageResponse>
      getMessageById(Long id) {
    Message message = getMessageByIdUseCase.execute(id);
    MessageResponse internal = MessageResponse.from(message);
    return ResponseEntity.ok(MessageMapper.toGenerated(internal));
  }

  @Override
  public ResponseEntity<com.sandbox.api.presentation.generated.model.MessageResponse>
      createMessage(com.sandbox.api.presentation.generated.model.MessageRequest messageRequest) {
    MessageRequest internal = MessageMapper.toInternal(messageRequest);
    Message message = createMessageUseCase.execute(internal.getCode(), internal.getContent());

    URI location =
        UriComponentsBuilder.fromPath("/api/messages/{id}")
            .buildAndExpand(message.getId())
            .toUri();

    MessageResponse response = MessageResponse.from(message);
    return ResponseEntity.created(location).body(MessageMapper.toGenerated(response));
  }

  @Override
  public ResponseEntity<com.sandbox.api.presentation.generated.model.MessageResponse>
      updateMessage(
          Long id, com.sandbox.api.presentation.generated.model.MessageRequest messageRequest) {
    MessageRequest internal = MessageMapper.toInternal(messageRequest);
    Message message = updateMessageUseCase.execute(id, internal.getCode(), internal.getContent());
    MessageResponse response = MessageResponse.from(message);
    return ResponseEntity.ok(MessageMapper.toGenerated(response));
  }

  @Override
  public ResponseEntity<Void> deleteMessage(Long id) {
    deleteMessageUseCase.execute(id);
    return ResponseEntity.noContent().build();
  }
}
