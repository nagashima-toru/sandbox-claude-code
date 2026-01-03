package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}
