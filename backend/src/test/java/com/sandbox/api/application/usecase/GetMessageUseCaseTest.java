package com.sandbox.api.application.usecase;

import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMessageUseCaseTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private GetMessageUseCase useCase;

    @Test
    void execute_whenMessageExists_returnsMessage() {
        // Arrange
        Message expected = new Message(1L, "hello", "Hello, World!");
        when(messageRepository.findByCode("hello")).thenReturn(Optional.of(expected));

        // Act
        Optional<Message> result = useCase.execute("hello");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getCode()).isEqualTo("hello");
        assertThat(result.get().getContent()).isEqualTo("Hello, World!");
        verify(messageRepository).findByCode("hello");
    }

    @Test
    void execute_whenMessageNotFound_returnsEmptyOptional() {
        // Arrange
        when(messageRepository.findByCode("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<Message> result = useCase.execute("nonexistent");

        // Assert
        assertThat(result).isEmpty();
        verify(messageRepository).findByCode("nonexistent");
    }

    @Test
    void execute_withDifferentCode_callsRepositoryWithSameCode() {
        // Arrange
        String code = "custom-code";
        Message message = new Message(2L, code, "Custom Message");
        when(messageRepository.findByCode(code)).thenReturn(Optional.of(message));

        // Act
        Optional<Message> result = useCase.execute(code);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo(code);
        verify(messageRepository).findByCode(code);
    }

    @Test
    void execute_withNullCode_delegatesToRepository() {
        // Arrange
        when(messageRepository.findByCode(null)).thenReturn(Optional.empty());

        // Act
        Optional<Message> result = useCase.execute(null);

        // Assert
        assertThat(result).isEmpty();
        verify(messageRepository).findByCode(null);
    }
}
