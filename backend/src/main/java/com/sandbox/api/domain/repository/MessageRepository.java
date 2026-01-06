package com.sandbox.api.domain.repository;

import com.sandbox.api.domain.model.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Optional<Message> findByCode(String code);
    List<Message> findAll();
    Optional<Message> findById(Long id);
    Message save(Message message);
    void deleteById(Long id);
    boolean existsByCode(String code);
}
