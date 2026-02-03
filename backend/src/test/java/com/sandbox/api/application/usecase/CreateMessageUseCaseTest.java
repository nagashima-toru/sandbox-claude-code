package com.sandbox.api.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateMessageUseCaseTest {
  @Mock private MessageRepository messageRepository;
  @InjectMocks private CreateMessageUseCase useCase;

  @Test
  void execute_withValidData_createsMessage() {
    // Arrange
    String code = "new-code";
    String content = "New Content";
    Message savedMessage = new Message(1L, code, content, LocalDateTime.now(), LocalDateTime.now());
    when(messageRepository.existsByCode(code)).thenReturn(false);
    when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
    // Act
    Message result = useCase.execute(code, content);
    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getCode()).isEqualTo(code);
    assertThat(result.getContent()).isEqualTo(content);
    verify(messageRepository).existsByCode(code);
    verify(messageRepository).save(any(Message.class));
  }

  @Test
  void execute_withDuplicateCode_throwsDuplicateMessageCodeException() {
    String code = "existing-code";
    String content = "Some Content";
    when(messageRepository.existsByCode(code)).thenReturn(true);
    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(code, content))
        .isInstanceOf(DuplicateMessageCodeException.class)
        .hasMessage("Message with code 'existing-code' already exists");
  }
}
