package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case for creating a new message. */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateMessageUseCase {

  private final MessageRepository messageRepository;

  /**
   * Creates a new message with the given code and content.
   *
   * @param code the unique code for the message
   * @param content the content of the message
   * @return the created message
   * @throws DuplicateMessageCodeException if a message with the given code already exists
   */
  @Transactional
  public Message execute(String code, String content) {
    log.debug("Creating message with code: {}", code);

    if (messageRepository.existsByCode(code)) {
      log.warn("Duplicate message code: {}", code);
      throw new DuplicateMessageCodeException(code);
    }

    Message message = Message.createNew(code, content);
    Message saved = messageRepository.save(message);
    log.info("Created message with id: {} and code: {}", saved.getId(), saved.getCode());
    return saved;
  }
}
