package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

  /**
   * Retrieves messages with pagination.
   *
   * @param page the page number (0-indexed)
   * @param size the number of items per page
   * @return a page of messages
   */
  @Transactional(readOnly = true)
  public Page<Message> execute(int page, int size) {
    log.debug("Fetching messages with pagination: page={}, size={}", page, size);
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Message> messages = messageRepository.findAll(pageable);
    log.debug("Found {} messages in page {}", messages.getNumberOfElements(), page);
    return messages;
  }
}
