package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import com.sandbox.api.domain.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case for updating an existing message. */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateMessageUseCase {

  private final MessageRepository messageRepository;

  /**
   * Updates an existing message with new code and content.
   *
   * @param id the ID of the message to update
   * @param code the new code for the message
   * @param content the new content for the message
   * @return the updated message
   * @throws MessageNotFoundException if no message exists with the given ID
   * @throws DuplicateMessageCodeException if the new code is already in use by another message
   */
  @Transactional
  public Message execute(Long id, String code, String content) {
    log.debug("Updating message with id: {}", id);

    Message existingMessage =
        messageRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.warn("Message not found with id: {}", id);
                  return new MessageNotFoundException(id);
                });

    if (!existingMessage.getCode().equals(code)) {
      if (messageRepository.existsByCode(code)) {
        log.warn("Duplicate message code: {}", LogSanitizer.sanitize(code));
        throw new DuplicateMessageCodeException(code);
      }
    }

    Message updatedMessage = existingMessage.update(code, content);
    Message saved = messageRepository.save(updatedMessage);
    log.info("Updated message with id: {}", saved.getId());
    return saved;
  }
}
