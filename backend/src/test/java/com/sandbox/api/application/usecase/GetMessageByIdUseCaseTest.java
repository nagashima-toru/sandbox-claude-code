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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMessageByIdUseCaseTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private GetMessageByIdUseCase useCase;

    @Test
    void execute_whenMessageExists_returnsMessage() {
        // Arrange
        Message expected = new Message(1L, "hello", "Hello, World!");
        when(messageRepository.findById(1L)).thenReturn(Optional.of(expected));

        // Act
        Message result = useCase.execute(1L);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(messageRepository).findById(1L);
    }

    @Test
    void execute_whenMessageNotFound_throwsMessageNotFoundException() {
        // Arrange
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(MessageNotFoundException.class)
                .hasMessage("Message with id 99 not found");
        verify(messageRepository).findById(99L);
    }
}
