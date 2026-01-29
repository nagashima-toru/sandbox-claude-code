package com.sandbox.api.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateMessageUseCaseTest {

  @Mock private MessageRepository messageRepository;

  @InjectMocks private UpdateMessageUseCase useCase;

  @Test
  void execute_withValidData_updatesMessage() {
    // Arrange
    Long id = 1L;
    String code = "updated-code";
    String content = "Updated Content";
    Message existingMessage = new Message(id, "old-code", "Old Content");
    Message updatedMessage = new Message(id, code, content);
    when(messageRepository.findById(id)).thenReturn(Optional.of(existingMessage));
    when(messageRepository.existsByCode(code)).thenReturn(false);
    when(messageRepository.save(any(Message.class))).thenReturn(updatedMessage);

    // Act
    Message result = useCase.execute(id, code, content);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCode()).isEqualTo(code);
    assertThat(result.getContent()).isEqualTo(content);
    verify(messageRepository).findById(id);
    verify(messageRepository).existsByCode(code);
    verify(messageRepository).save(any(Message.class));
  }

  @Test
  void execute_withSameCode_updatesSuccessfully() {
    // Arrange
    Long id = 1L;
    String code = "same-code";
    String newContent = "Updated Content";
    Message existingMessage = new Message(id, code, "Old Content");
    Message updatedMessage = new Message(id, code, newContent);
    when(messageRepository.findById(id)).thenReturn(Optional.of(existingMessage));
    when(messageRepository.save(any(Message.class))).thenReturn(updatedMessage);

    // Act
    Message result = useCase.execute(id, code, newContent);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCode()).isEqualTo(code);
    assertThat(result.getContent()).isEqualTo(newContent);
    verify(messageRepository).findById(id);
    verify(messageRepository).save(any(Message.class));
  }

  @Test
  void execute_withNonexistentId_throwsMessageNotFoundException() {
    // Arrange
    Long id = 99L;
    when(messageRepository.findById(id)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(id, "code", "content"))
        .isInstanceOf(MessageNotFoundException.class)
        .hasMessage("Message with id 99 not found");
    verify(messageRepository).findById(id);
  }

  @Test
  void execute_withDuplicateCode_throwsDuplicateMessageCodeException() {
    // Arrange
    Long id = 1L;
    String newCode = "duplicate-code";
    Message existingMessage = new Message(id, "old-code", "Old Content");
    when(messageRepository.findById(id)).thenReturn(Optional.of(existingMessage));
    when(messageRepository.existsByCode(newCode)).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(id, newCode, "content"))
        .isInstanceOf(DuplicateMessageCodeException.class)
        .hasMessage("Message with code 'duplicate-code' already exists");
    verify(messageRepository).findById(id);
    verify(messageRepository).existsByCode(newCode);
  }
}
