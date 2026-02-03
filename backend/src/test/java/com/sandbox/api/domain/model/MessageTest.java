package com.sandbox.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MessageTest {
  @Test
  void constructor_withAllParameters_createsMessage() {
    // Arrange & Act
    Message message =
        new Message(1L, "test-code", "Test Content", LocalDateTime.now(), LocalDateTime.now());
    // Assert
    assertThat(message.getId()).isEqualTo(1L);
    assertThat(message.getCode()).isEqualTo("test-code");
    assertThat(message.getContent()).isEqualTo("Test Content");
  }

  @Test
  void defaultConstructor_createsEmptyMessage() {
    Message message = new Message();
    assertThat(message.getId()).isNull();
    assertThat(message.getCode()).isNull();
    assertThat(message.getContent()).isNull();
  }

  @Test
  void setters_updateFieldsCorrectly() {
    // Arrange
    Message message = new Message();
    // Act
    message.setId(100L);
    message.setCode("new-code");
    message.setContent("New Content");
    // Assert
    assertThat(message.getId()).isEqualTo(100L);
    assertThat(message.getCode()).isEqualTo("new-code");
    assertThat(message.getContent()).isEqualTo("New Content");
  }

  @Test
  void setId_withNullValue_setsNull() {
    Message message = new Message(1L, "code", "content", LocalDateTime.now(), LocalDateTime.now());
    message.setId(null);
    assertThat(message.getId()).isNull();
  }

  @Test
  void setCode_withNullValue_setsNull() {
    Message message = new Message(1L, "code", "content", LocalDateTime.now(), LocalDateTime.now());
    message.setCode(null);
    assertThat(message.getCode()).isNull();
  }

  @Test
  void setContent_withNullValue_setsNull() {
    Message message = new Message(1L, "code", "content", LocalDateTime.now(), LocalDateTime.now());
    message.setContent(null);
    assertThat(message.getContent()).isNull();
  }

  @Test
  void getCreatedAt_returnsCreatedTimestamp() {
    LocalDateTime now = LocalDateTime.now();
    Message message = new Message(1L, "code", "content", now, LocalDateTime.now());
    assertThat(message.getCreatedAt()).isEqualTo(now);
  }

  @Test
  void getUpdatedAt_returnsUpdatedTimestamp() {
    LocalDateTime now = LocalDateTime.now();
    Message message = new Message(1L, "code", "content", LocalDateTime.now(), now);
    assertThat(message.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  void setCreatedAt_updatesCreatedTimestamp() {
    Message message = new Message();
    LocalDateTime now = LocalDateTime.now();
    message.setCreatedAt(now);
    assertThat(message.getCreatedAt()).isEqualTo(now);
  }

  @Test
  void setUpdatedAt_updatesUpdatedTimestamp() {
    Message message = new Message();
    LocalDateTime now = LocalDateTime.now();
    message.setUpdatedAt(now);
    assertThat(message.getUpdatedAt()).isEqualTo(now);
  }
}
