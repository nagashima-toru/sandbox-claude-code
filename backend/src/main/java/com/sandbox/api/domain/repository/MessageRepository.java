package com.sandbox.api.domain.repository;

import com.sandbox.api.domain.model.Message;

import java.util.Optional;

public interface MessageRepository {
    Optional<Message> findByCode(String code);
}
