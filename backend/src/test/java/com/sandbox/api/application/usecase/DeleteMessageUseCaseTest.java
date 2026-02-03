package com.sandbox.api.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteMessageUseCaseTest {
  @Mock private MessageRepository messageRepository;
  @InjectMocks private DeleteMessageUseCase useCase;

  @Test
  void execute_withExistingId_deletesMessage() {
    // Arrange
    Long id = 1L;
    when(messageRepository.existsById(id)).thenReturn(true);
    // Act
    useCase.execute(id);
    // Assert
    verify(messageRepository).existsById(id);
    verify(messageRepository).deleteById(id);
  }

  @Test
  void execute_withNonexistentId_throwsMessageNotFoundException() {
    Long id = 99L;
    when(messageRepository.existsById(id)).thenReturn(false);
    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(id))
        .isInstanceOf(MessageNotFoundException.class)
        .hasMessage("Message with id 99 not found");
  }
}
