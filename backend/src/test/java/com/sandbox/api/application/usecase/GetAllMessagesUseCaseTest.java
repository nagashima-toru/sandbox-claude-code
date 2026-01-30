package com.sandbox.api.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.sandbox.api.domain.model.Message;
import com.sandbox.api.domain.repository.MessageRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class GetAllMessagesUseCaseTest {
  @Mock private MessageRepository messageRepository;
  @InjectMocks private GetAllMessagesUseCase useCase;
  @Test
  void execute_whenMessagesExist_returnsAllMessages() {
    // Arrange
    List<Message> expected =
        Arrays.asList(new Message(1L, "code1", "Content 1", LocalDateTime.now(), LocalDateTime.now()), new Message(2L, "code2", "Content 2", LocalDateTime.now(), LocalDateTime.now()));
    when(messageRepository.findAll()).thenReturn(expected);
    // Act
    List<Message> result = useCase.execute();
    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).isEqualTo(expected);
    verify(messageRepository).findAll();
  }
  void execute_whenNoMessages_returnsEmptyList() {
    when(messageRepository.findAll()).thenReturn(Collections.emptyList());
    assertThat(result).isEmpty();
}
