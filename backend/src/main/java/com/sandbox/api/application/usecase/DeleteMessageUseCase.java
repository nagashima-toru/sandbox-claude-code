package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteMessageUseCase {

    private final MessageRepository messageRepository;

    @Transactional
    public void execute(Long id) {
        log.debug("Deleting message with id: {}", id);

        if (!messageRepository.existsById(id)) {
            log.warn("Message not found with id: {}", id);
            throw new MessageNotFoundException(id);
        }

        messageRepository.deleteById(id);
        log.info("Deleted message with id: {}", id);
    }
}
