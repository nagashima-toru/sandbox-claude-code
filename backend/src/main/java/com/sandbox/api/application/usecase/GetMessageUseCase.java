package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetMessageUseCase {

    private final MessageRepository messageRepository;

    public GetMessageUseCase(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Optional<Message> execute(String code) {
        return messageRepository.findByCode(code);
    }
}
