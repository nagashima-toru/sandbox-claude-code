package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateMessageUseCase {

    private final MessageRepository messageRepository;

    public UpdateMessageUseCase(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message execute(Long id, String code, String content) {
        Message existingMessage = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));

        if (!existingMessage.getCode().equals(code)) {
            if (messageRepository.existsByCode(code)) {
                throw new DuplicateMessageCodeException(code);
            }
        }

        existingMessage.setCode(code);
        existingMessage.setContent(content);
        return messageRepository.save(existingMessage);
    }
}
