package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class GetMessageByIdUseCase {

    private final MessageRepository messageRepository;

    public GetMessageByIdUseCase(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message execute(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
    }
}
