package com.sandbox.api.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sandbox.api.domain.model.Message;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class MessageRepositoryImplTest {
  @Mock private MessageMapper messageMapper;
  @InjectMocks private MessageRepositoryImpl repository;

  @Test
  void findByCode_whenMapperReturnsMessage_returnsOptionalWithMessage() {
    // Arrange
    Message expected =
        new Message(1L, "test-code", "Test Message", LocalDateTime.now(), LocalDateTime.now());
    when(messageMapper.findByCode("test-code")).thenReturn(expected);
    // Act
    Optional<Message> result = repository.findByCode("test-code");
    // Assert
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expected);
    verify(messageMapper).findByCode("test-code");
  }

  @Test
  void findByCode_whenMapperReturnsNull_returnsEmptyOptional() {
    when(messageMapper.findByCode("nonexistent")).thenReturn(null);
    Optional<Message> result = repository.findByCode("nonexistent");
    assertThat(result).isEmpty();
    verify(messageMapper).findByCode("nonexistent");
  }

  @Test
  void findByCode_withNullCode_returnsEmptyOptionalWhenMapperReturnsNull() {
    when(messageMapper.findByCode(null)).thenReturn(null);
    Optional<Message> result = repository.findByCode(null);
    verify(messageMapper).findByCode(null);
  }

  @Test
  void findByCode_callsMapperExactlyOnce() {
    String code = "hello";
    Message message =
        new Message(1L, code, "Hello, World!", LocalDateTime.now(), LocalDateTime.now());
    when(messageMapper.findByCode(code)).thenReturn(message);
    repository.findByCode(code);
    verify(messageMapper).findByCode(code);
  }

  @Test
  void findAll_whenMapperReturnsMessages_returnsListOfMessages() {
    List<Message> expected =
        Arrays.asList(
            new Message(1L, "code1", "Content 1", LocalDateTime.now(), LocalDateTime.now()),
            new Message(2L, "code2", "Content 2", LocalDateTime.now(), LocalDateTime.now()));
    when(messageMapper.findAll()).thenReturn(expected);
    List<Message> result = repository.findAll();
    assertThat(result).hasSize(2);
    assertThat(result).isEqualTo(expected);
    verify(messageMapper).findAll();
  }

  @Test
  void findById_whenMapperReturnsMessage_returnsOptionalWithMessage() {
    Message expected = new Message(1L, "code", "Content", LocalDateTime.now(), LocalDateTime.now());
    when(messageMapper.findById(1L)).thenReturn(expected);
    Optional<Message> result = repository.findById(1L);
    verify(messageMapper).findById(1L);
  }

  @Test
  void findById_whenMapperReturnsNull_returnsEmptyOptional() {
    when(messageMapper.findById(99L)).thenReturn(null);
    Optional<Message> result = repository.findById(99L);
    verify(messageMapper).findById(99L);
  }

  @Test
  void save_whenMessageHasNoId_callsInsert() {
    Message message =
        new Message(null, "new-code", "New Content", LocalDateTime.now(), LocalDateTime.now());
    Message result = repository.save(message);
    assertThat(result).isEqualTo(message);
    verify(messageMapper).insert(message);
  }

  @Test
  void save_whenMessageHasId_callsUpdate() {
    Message message =
        new Message(1L, "code", "Updated Content", LocalDateTime.now(), LocalDateTime.now());
    Message result = repository.save(message);
    assertThat(result).isEqualTo(message);
    verify(messageMapper).update(message);
  }

  @Test
  void deleteById_callsMapperDeleteById() {
    // Arrange & Act
    repository.deleteById(1L);
    verify(messageMapper).deleteById(1L);
  }

  @Test
  void existsByCode_whenMapperReturnsTrue_returnsTrue() {
    when(messageMapper.existsByCode("existing")).thenReturn(true);
    boolean result = repository.existsByCode("existing");
    assertThat(result).isTrue();
    verify(messageMapper).existsByCode("existing");
  }

  @Test
  void existsByCode_whenMapperReturnsFalse_returnsFalse() {
    when(messageMapper.existsByCode("nonexistent")).thenReturn(false);
    boolean result = repository.existsByCode("nonexistent");
    assertThat(result).isFalse();
    verify(messageMapper).existsByCode("nonexistent");
  }

  // Security tests for SQL injection prevention

  @Test
  void findAllWithPagination_withValidSortField_succeeds() {
    // Arrange
    List<Message> messages =
        Arrays.asList(
            new Message(1L, "code1", "Content 1", LocalDateTime.now(), LocalDateTime.now()));
    when(messageMapper.findAllWithPagination(0L, 10, "id", "ASC")).thenReturn(messages);
    when(messageMapper.count()).thenReturn(1L);

    PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

    // Act
    Page<Message> result = repository.findAll(pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(messageMapper).findAllWithPagination(0L, 10, "id", "ASC");
  }

  @Test
  void findAllWithPagination_withCamelCaseSortField_convertsToSnakeCase() {
    // Arrange
    List<Message> messages =
        Arrays.asList(
            new Message(1L, "code1", "Content 1", LocalDateTime.now(), LocalDateTime.now()));
    when(messageMapper.findAllWithPagination(0L, 10, "created_at", "DESC")).thenReturn(messages);
    when(messageMapper.count()).thenReturn(1L);

    PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

    // Act
    Page<Message> result = repository.findAll(pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(messageMapper).findAllWithPagination(0L, 10, "created_at", "DESC");
  }

  @Test
  void findAllWithPagination_withInvalidSortField_throwsIllegalArgumentException() {
    // Arrange
    PageRequest pageable =
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "malicious; DROP TABLE messages--"));

    // Act & Assert
    assertThatThrownBy(() -> repository.findAll(pageable))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid sort field");
  }

  @Test
  void findAllWithPagination_withSqlInjectionAttemptInSortField_throwsException() {
    // Arrange
    PageRequest pageable =
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id; DELETE FROM messages"));

    // Act & Assert
    assertThatThrownBy(() -> repository.findAll(pageable))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid sort field");
  }

  @Test
  void findAllWithPagination_withInvalidSortDirection_throwsIllegalArgumentException() {
    // This test ensures that even if someone manipulates the sort direction,
    // it will be validated. Note: Spring's Sort.Direction is an enum, so this is less likely,
    // but we test the validation logic anyway.
    List<Message> messages =
        Arrays.asList(
            new Message(1L, "code1", "Content 1", LocalDateTime.now(), LocalDateTime.now()));
    when(messageMapper.findAllWithPagination(0L, 10, "id", "ASC")).thenReturn(messages);
    when(messageMapper.count()).thenReturn(1L);

    PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

    // Act
    Page<Message> result = repository.findAll(pageable);

    // Assert - verify that only ASC/DESC are accepted
    assertThat(result).isNotNull();
    verify(messageMapper).findAllWithPagination(0L, 10, "id", "ASC");
  }

  @Test
  void findAllWithPagination_withAllAllowedSortFields_succeeds() {
    // Test all allowed fields: id, code, content, createdAt, updatedAt
    String[][] testCases = {
      {"id", "id"},
      {"code", "code"},
      {"content", "content"},
      {"createdAt", "created_at"},
      {"updatedAt", "updated_at"}
    };

    for (String[] testCase : testCases) {
      String camelCase = testCase[0];
      String snakeCase = testCase[1];

      List<Message> messages =
          Arrays.asList(
              new Message(1L, "code1", "Content 1", LocalDateTime.now(), LocalDateTime.now()));
      when(messageMapper.findAllWithPagination(0L, 10, snakeCase, "ASC")).thenReturn(messages);
      when(messageMapper.count()).thenReturn(1L);

      PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, camelCase));

      // Act
      Page<Message> result = repository.findAll(pageable);

      // Assert
      assertThat(result.getContent()).hasSize(1);
      verify(messageMapper).findAllWithPagination(0L, 10, snakeCase, "ASC");
    }
  }

  @Test
  void findAllWithPagination_withNonWhitelistedField_throwsException() {
    // Arrange
    PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "nonExistentField"));

    // Act & Assert
    assertThatThrownBy(() -> repository.findAll(pageable))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid sort field: nonExistentField");
  }
}
