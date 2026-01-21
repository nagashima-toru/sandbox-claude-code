package com.sandbox.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageTest {

  @Test
  void constructor_withAllParameters_createsMessage() {
    // Arrange & Act
    Message message = new Message(1L, "test-code", "Test Content");

    // Assert
    assertThat(message.getId()).isEqualTo(1L);
    assertThat(message.getCode()).isEqualTo("test-code");
    assertThat(message.getContent()).isEqualTo("Test Content");
  }

  @Test
  void defaultConstructor_createsEmptyMessage() {
    // Arrange & Act
    Message message = new Message();

    // Assert
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
    // Arrange
    Message message = new Message(1L, "code", "content");

    // Act
    message.setId(null);

    // Assert
    assertThat(message.getId()).isNull();
  }

  @Test
  void setCode_withNullValue_setsNull() {
    // Arrange
    Message message = new Message(1L, "code", "content");

    // Act
    message.setCode(null);

    // Assert
    assertThat(message.getCode()).isNull();
  }

  @Test
  void setContent_withNullValue_setsNull() {
    // Arrange
    Message message = new Message(1L, "code", "content");

    // Act
    message.setContent(null);

    // Assert
    assertThat(message.getContent()).isNull();
  }
}
