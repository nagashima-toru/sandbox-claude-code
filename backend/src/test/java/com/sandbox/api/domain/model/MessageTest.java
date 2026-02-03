package com.sandbox.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MessageTest {

  @Test
  void createNew_withValidData_createsMessage() {
    // Arrange & Act
    Message message = Message.createNew("test-code", "Test Content");

    // Assert
    assertThat(message.getCode()).isEqualTo("test-code");
    assertThat(message.getContent()).isEqualTo("Test Content");
    assertThat(message.getCreatedAt()).isNotNull();
    assertThat(message.getId()).isNull();
    assertThat(message.getUpdatedAt()).isNull();
  }

  @Test
  void createNew_withNullCode_throwsException() {
    assertThatThrownBy(() -> Message.createNew(null, "Content"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Code cannot be null or blank");
  }

  @Test
  void createNew_withBlankCode_throwsException() {
    assertThatThrownBy(() -> Message.createNew("   ", "Content"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Code cannot be null or blank");
  }

  @Test
  void createNew_withNullContent_throwsException() {
    assertThatThrownBy(() -> Message.createNew("CODE", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Content cannot be null or blank");
  }

  @Test
  void createNew_withBlankContent_throwsException() {
    assertThatThrownBy(() -> Message.createNew("CODE", "   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Content cannot be null or blank");
  }

  @Test
  void createNew_withCodeTooLong_throwsException() {
    String longCode = "a".repeat(51);
    assertThatThrownBy(() -> Message.createNew(longCode, "Content"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Code must be 50 characters or less");
  }

  @Test
  void createNew_withContentTooLong_throwsException() {
    String longContent = "a".repeat(501);
    assertThatThrownBy(() -> Message.createNew("CODE", longContent))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Content must be 500 characters or less");
  }

  @Test
  void update_withValidData_createsNewInstance() {
    // Arrange
    Message original = Message.createNew("CODE001", "Original Content");

    // Act
    Message updated = original.update("CODE002", "Updated Content");

    // Assert - Original instance is unchanged
    assertThat(original.getCode()).isEqualTo("CODE001");
    assertThat(original.getContent()).isEqualTo("Original Content");
    assertThat(original.getUpdatedAt()).isNull();

    // Assert - New instance has updated values
    assertThat(updated.getCode()).isEqualTo("CODE002");
    assertThat(updated.getContent()).isEqualTo("Updated Content");
    assertThat(updated.getUpdatedAt()).isNotNull();
    assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());

    // Assert - They are different instances
    assertThat(updated).isNotSameAs(original);
  }

  @Test
  void update_withInvalidCode_throwsException() {
    Message message = Message.createNew("CODE", "Content");

    assertThatThrownBy(() -> message.update(null, "New Content"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Code cannot be null or blank");
  }

  @Test
  void update_withInvalidContent_throwsException() {
    Message message = Message.createNew("CODE", "Content");

    assertThatThrownBy(() -> message.update("NEW_CODE", ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Content cannot be null or blank");
  }

  @Test
  void withId_createsNewInstanceWithId() {
    // Arrange
    Message original = Message.createNew("CODE", "Content");

    // Act
    Message withId = original.withId(123L);

    // Assert - Original instance is unchanged
    assertThat(original.getId()).isNull();

    // Assert - New instance has ID
    assertThat(withId.getId()).isEqualTo(123L);
    assertThat(withId.getCode()).isEqualTo(original.getCode());
    assertThat(withId.getContent()).isEqualTo(original.getContent());

    // Assert - They are different instances
    assertThat(withId).isNotSameAs(original);
  }

  @Test
  void builder_createsMessage() {
    // Arrange & Act
    Message message =
        Message.builder().id(1L).code("test-code").content("Test Content").build();

    // Assert
    assertThat(message.getId()).isEqualTo(1L);
    assertThat(message.getCode()).isEqualTo("test-code");
    assertThat(message.getContent()).isEqualTo("Test Content");
  }

  @Test
  void toBuilder_createsNewInstanceWithModifications() {
    // Arrange
    Message original = Message.createNew("CODE", "Content").withId(1L);

    // Act
    Message modified = original.toBuilder().content("Modified Content").build();

    // Assert - Original is unchanged
    assertThat(original.getContent()).isEqualTo("Content");

    // Assert - New instance has modifications
    assertThat(modified.getContent()).isEqualTo("Modified Content");
    assertThat(modified.getId()).isEqualTo(original.getId());
    assertThat(modified.getCode()).isEqualTo(original.getCode());

    // Assert - They are different instances
    assertThat(modified).isNotSameAs(original);
  }

  @Test
  void equals_basedOnId() {
    Message message1 = Message.createNew("CODE1", "Content1").withId(1L);
    Message message2 = Message.createNew("CODE2", "Content2").withId(1L);
    Message message3 = Message.createNew("CODE1", "Content1").withId(2L);

    // Same ID should be equal
    assertThat(message1).isEqualTo(message2);

    // Different ID should not be equal
    assertThat(message1).isNotEqualTo(message3);
  }
}
