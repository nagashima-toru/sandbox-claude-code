package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case for retrieving all messages. */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetAllMessagesUseCase {

  private final MessageRepository messageRepository;

  /**
   * Retrieves all messages from the repository.
   *
   * @return a list of all messages
   */
  @Transactional(readOnly = true)
  public List<Message> execute() {
    log.debug("Fetching all messages");
    List<Message> messages = messageRepository.findAll();
    log.debug("Found {} messages", messages.size());
    return messages;
  }
}
