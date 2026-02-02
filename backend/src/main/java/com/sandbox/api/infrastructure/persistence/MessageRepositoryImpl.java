package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/** MyBatis implementation of the MessageRepository interface. */
@Repository
public class MessageRepositoryImpl implements MessageRepository {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "code", "content", "created_at", "updated_at");

  private static final Set<String> ALLOWED_SORT_DIRECTIONS = Set.of("ASC", "DESC");

  private final MessageMapper messageMapper;

  public MessageRepositoryImpl(MessageMapper messageMapper) {
    this.messageMapper = messageMapper;
  }

  @Override
  public Optional<Message> findByCode(String code) {
    return Optional.ofNullable(messageMapper.findByCode(code));
  }

  @Override
  public List<Message> findAll() {
    return messageMapper.findAll();
  }

  @Override
  public Page<Message> findAll(Pageable pageable) {
    long offset = pageable.getOffset();
    int limit = pageable.getPageSize();
    String sortField = "created_at";
    String sortDirection = "DESC";

    if (pageable.getSort().isSorted()) {
      var order = pageable.getSort().iterator().next();
      sortField = validateSortField(order.getProperty());
      sortDirection = validateSortDirection(order.getDirection().name());
    }

    List<Message> messages =
        messageMapper.findAllWithPagination(offset, limit, sortField, sortDirection);
    long total = messageMapper.count();

    return new PageImpl<>(messages, pageable, total);
  }

  @Override
  public Optional<Message> findById(Long id) {
    return Optional.ofNullable(messageMapper.findById(id));
  }

  @Override
  public Message save(Message message) {
    if (message.getId() == null) {
      messageMapper.insert(message);
    } else {
      messageMapper.update(message);
    }
    return message;
  }

  @Override
  public void deleteById(Long id) {
    messageMapper.deleteById(id);
  }

  @Override
  public boolean existsByCode(String code) {
    return messageMapper.existsByCode(code);
  }

  @Override
  public boolean existsById(Long id) {
    return messageMapper.existsById(id);
  }

  private String validateSortField(String field) {
    String snakeCase = camelToSnake(field);
    if (!ALLOWED_SORT_FIELDS.contains(snakeCase)) {
      throw new IllegalArgumentException("Invalid sort field: " + field);
    }
    return snakeCase;
  }

  private String validateSortDirection(String direction) {
    String upper = direction.toUpperCase();
    if (!ALLOWED_SORT_DIRECTIONS.contains(upper)) {
      throw new IllegalArgumentException("Invalid sort direction: " + direction);
    }
    return upper;
  }

  private String camelToSnake(String camelCase) {
    return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }
}
