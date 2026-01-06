package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteMessageUseCaseTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private DeleteMessageUseCase useCase;

    @Test
    void execute_withExistingId_deletesMessage() {
        // Arrange
        Long id = 1L;
        Message message = new Message(id, "code", "content");
        when(messageRepository.findById(id)).thenReturn(Optional.of(message));

        // Act
        useCase.execute(id);

        // Assert
        verify(messageRepository).findById(id);
        verify(messageRepository).deleteById(id);
    }

    @Test
    void execute_withNonexistentId_throwsMessageNotFoundException() {
        // Arrange
        Long id = 99L;
        when(messageRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(id))
                .isInstanceOf(MessageNotFoundException.class)
                .hasMessage("Message with id 99 not found");
        verify(messageRepository).findById(id);
    }
}
