package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case for retrieving a message by its ID. */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetMessageByIdUseCase {

  private final MessageRepository messageRepository;

  /**
   * Retrieves a message by its ID.
   *
   * @param id the ID of the message to retrieve
   * @return the message with the given ID
   * @throws MessageNotFoundException if no message exists with the given ID
   */
  @Transactional(readOnly = true)
  public Message execute(Long id) {
    log.debug("Fetching message with id: {}", id);
    return messageRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.warn("Message not found with id: {}", id);
              return new MessageNotFoundException(id);
            });
  }
}
