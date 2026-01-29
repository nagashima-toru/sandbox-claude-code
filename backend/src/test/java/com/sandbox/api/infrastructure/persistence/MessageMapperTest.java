package com.sandbox.api.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.sandbox.api.domain.model.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
class MessageMapperTest {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private MessageMapper messageMapper;

  @Test
  void findByCode_withExistingCode_returnsMessage() {
    // Arrange & Act
    Message result = messageMapper.findByCode("hello");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCode()).isEqualTo("hello");
    assertThat(result.getContent()).isEqualTo("Hello, World!");
    assertThat(result.getId()).isNotNull();
  }

  @Test
  void findByCode_withNonExistentCode_returnsNull() {
    // Arrange & Act
    Message result = messageMapper.findByCode("nonexistent");

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void findByCode_withNullCode_returnsNull() {
    // Arrange & Act
    Message result = messageMapper.findByCode(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void findByCode_withEmptyCode_returnsNull() {
    // Arrange & Act
    Message result = messageMapper.findByCode("");

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void findByCode_isCaseSensitive() {
    // Arrange & Act
    Message result = messageMapper.findByCode("HELLO");

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void findAll_returnsAllMessagesInOrder() {
    // Arrange & Act
    var results = messageMapper.findAll();

    // Assert
    assertThat(results).isNotEmpty();
    assertThat(results).hasSizeGreaterThanOrEqualTo(1);
    assertThat(results.get(0).getCode()).isEqualTo("hello");
  }

  @Test
  void findById_withExistingId_returnsMessage() {
    // Arrange
    Message existing = messageMapper.findByCode("hello");

    // Act
    Message result = messageMapper.findById(existing.getId());

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(existing.getId());
    assertThat(result.getCode()).isEqualTo("hello");
  }

  @Test
  void findById_withNonExistentId_returnsNull() {
    // Arrange & Act
    Message result = messageMapper.findById(99999L);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void insert_withValidMessage_insertsAndGeneratesId() {
    // Arrange
    Message message = new Message(null, "test-insert", "Test Insert Content");

    // Act
    messageMapper.insert(message);

    // Assert
    assertThat(message.getId()).isNotNull();
    Message inserted = messageMapper.findByCode("test-insert");
    assertThat(inserted).isNotNull();
    assertThat(inserted.getContent()).isEqualTo("Test Insert Content");
  }

  @Test
  void update_withExistingMessage_updatesSuccessfully() {
    // Arrange
    Message message = new Message(null, "test-update", "Original Content");
    messageMapper.insert(message);
    message.setContent("Updated Content");

    // Act
    messageMapper.update(message);

    // Assert
    Message updated = messageMapper.findById(message.getId());
    assertThat(updated).isNotNull();
    assertThat(updated.getContent()).isEqualTo("Updated Content");
  }

  @Test
  void deleteById_withExistingId_deletesMessage() {
    // Arrange
    Message message = new Message(null, "test-delete", "To Be Deleted");
    messageMapper.insert(message);
    Long id = message.getId();

    // Act
    messageMapper.deleteById(id);

    // Assert
    Message deleted = messageMapper.findById(id);
    assertThat(deleted).isNull();
  }

  @Test
  void existsByCode_withExistingCode_returnsTrue() {
    // Arrange & Act
    boolean exists = messageMapper.existsByCode("hello");

    // Assert
    assertThat(exists).isTrue();
  }

  @Test
  void existsByCode_withNonExistentCode_returnsFalse() {
    // Arrange & Act
    boolean exists = messageMapper.existsByCode("nonexistent");

    // Assert
    assertThat(exists).isFalse();
  }
}
