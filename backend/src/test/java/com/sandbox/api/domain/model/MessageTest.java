package com.sandbox.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
class MessageTest {
  @Test
  void constructor_withAllParameters_createsMessage() {
    // Arrange & Act
    Message message = new Message(1L, "test-code", "Test Content", LocalDateTime.now(), LocalDateTime.now());
    // Assert
    assertThat(message.getId()).isEqualTo(1L);
    assertThat(message.getCode()).isEqualTo("test-code");
    assertThat(message.getContent()).isEqualTo("Test Content");
  }
  void defaultConstructor_createsEmptyMessage() {
    Message message = new Message();
    assertThat(message.getId()).isNull();
    assertThat(message.getCode()).isNull();
    assertThat(message.getContent()).isNull();
  void setters_updateFieldsCorrectly() {
    // Arrange
    // Act
    message.setId(100L);
    message.setCode("new-code");
    message.setContent("New Content");
    assertThat(message.getId()).isEqualTo(100L);
    assertThat(message.getCode()).isEqualTo("new-code");
    assertThat(message.getContent()).isEqualTo("New Content");
  void setId_withNullValue_setsNull() {
    Message message = new Message(1L, "code", "content", LocalDateTime.now(), LocalDateTime.now());
    message.setId(null);
  void setCode_withNullValue_setsNull() {
    message.setCode(null);
  void setContent_withNullValue_setsNull() {
    message.setContent(null);
}
