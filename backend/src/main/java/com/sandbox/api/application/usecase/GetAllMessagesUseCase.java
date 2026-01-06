package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllMessagesUseCase {

    private final MessageRepository messageRepository;

    public GetAllMessagesUseCase(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<Message> execute() {
        return messageRepository.findAll();
    }
}
