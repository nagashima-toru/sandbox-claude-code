package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateMessageUseCase {

    private final MessageRepository messageRepository;

    public CreateMessageUseCase(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message execute(String code, String content) {
        if (messageRepository.existsByCode(code)) {
            throw new DuplicateMessageCodeException(code);
        }

        Message message = new Message(null, code, content);
        return messageRepository.save(message);
    }
}
