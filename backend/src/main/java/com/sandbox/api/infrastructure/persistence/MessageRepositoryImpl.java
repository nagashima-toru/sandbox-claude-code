package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** MyBatis implementation of the MessageRepository interface. */
@Repository
public class MessageRepositoryImpl implements MessageRepository {

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
}
