package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateMessageUseCase {

    private final MessageRepository messageRepository;

    @Transactional
    public Message execute(Long id, String code, String content) {
        log.debug("Updating message with id: {}", id);

        Message existingMessage = messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Message not found with id: {}", id);
                    return new MessageNotFoundException(id);
                });

        if (!existingMessage.getCode().equals(code)) {
            if (messageRepository.existsByCode(code)) {
                log.warn("Duplicate message code: {}", code);
                throw new DuplicateMessageCodeException(code);
            }
        }

        existingMessage.setCode(code);
        existingMessage.setContent(content);
        Message saved = messageRepository.save(existingMessage);
        log.info("Updated message with id: {}", saved.getId());
        return saved;
    }
}
