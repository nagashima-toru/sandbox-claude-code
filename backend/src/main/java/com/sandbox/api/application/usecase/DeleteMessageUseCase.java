package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteMessageUseCase {

    private final MessageRepository messageRepository;

    public DeleteMessageUseCase(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void execute(Long id) {
        if (!messageRepository.findById(id).isPresent()) {
            throw new MessageNotFoundException(id);
        }
        messageRepository.deleteById(id);
    }
}
