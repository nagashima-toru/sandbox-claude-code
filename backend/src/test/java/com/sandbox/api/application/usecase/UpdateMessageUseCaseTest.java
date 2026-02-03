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
import java.time.LocalDateTime;
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
    Message existingMessage =
        Message.builder()
            .id(id)
            .code("old-code")
            .content("Old Content")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    Message updatedMessage =
        Message.builder()
            .id(id)
            .code(code)
            .content(content)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
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
    Long id = 1L;
    String code = "same-code";
    String newContent = "Updated Content";
    Message existingMessage =
        Message.builder()
            .id(id)
            .code(code)
            .content("Old Content")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    Message updatedMessage =
        Message.builder()
            .id(id)
            .code(code)
            .content(newContent)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    when(messageRepository.findById(id)).thenReturn(Optional.of(existingMessage));
    when(messageRepository.save(any(Message.class))).thenReturn(updatedMessage);
    Message result = useCase.execute(id, code, newContent);
    assertThat(result.getContent()).isEqualTo(newContent);
  }

  @Test
  void execute_withNonexistentId_throwsMessageNotFoundException() {
    Long id = 99L;
    when(messageRepository.findById(id)).thenReturn(Optional.empty());
    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(id, "code", "content"))
        .isInstanceOf(MessageNotFoundException.class)
        .hasMessage("Message with id 99 not found");
  }

  @Test
  void execute_withDuplicateCode_throwsDuplicateMessageCodeException() {
    Long id = 1L;
    String newCode = "duplicate-code";
    Message existingMessage =
        Message.builder()
            .id(id)
            .code("old-code")
            .content("Old Content")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    when(messageRepository.findById(id)).thenReturn(Optional.of(existingMessage));
    when(messageRepository.existsByCode(newCode)).thenReturn(true);
    assertThatThrownBy(() -> useCase.execute(id, newCode, "content"))
        .isInstanceOf(DuplicateMessageCodeException.class)
        .hasMessage("Message with code 'duplicate-code' already exists");
    verify(messageRepository).existsByCode(newCode);
  }
}
