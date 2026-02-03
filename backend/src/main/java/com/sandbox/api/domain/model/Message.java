package com.sandbox.api.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Immutable domain model representing a message.
 *
 * <p>This class follows Clean Architecture principles by ensuring immutability and encapsulating
 * domain logic within the domain layer.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Message {
  private final Long id;
  private final String code;
  private final String content;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  private static final int MAX_CODE_LENGTH = 50;
  private static final int MAX_CONTENT_LENGTH = 500;

  /**
   * Creates a new message with the given code and content.
   *
   * @param code the unique code for the message
   * @param content the content of the message
   * @return a new Message instance
   * @throws IllegalArgumentException if code or content is null, blank, or exceeds maximum length
   */
  public static Message createNew(String code, String content) {
    validateCode(code);
    validateContent(content);

    return Message.builder().code(code).content(content).createdAt(LocalDateTime.now()).build();
  }

  /**
   * Creates a new Message instance with updated code and content.
   *
   * @param newCode the new code
   * @param newContent the new content
   * @return a new Message instance with updated values
   * @throws IllegalArgumentException if newCode or newContent is invalid
   */
  public Message update(String newCode, String newContent) {
    validateCode(newCode);
    validateContent(newContent);

    return this.toBuilder()
        .code(newCode)
        .content(newContent)
        .updatedAt(LocalDateTime.now())
        .build();
  }

  /**
   * Creates a new Message instance with the specified ID.
   *
   * @param newId the new ID
   * @return a new Message with the specified ID
   */
  public Message withId(Long newId) {
    return this.toBuilder().id(newId).build();
  }

  /**
   * Validates the message code.
   *
   * @param code the code to validate
   * @throws IllegalArgumentException if code is invalid
   */
  private static void validateCode(String code) {
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("Code cannot be null or blank");
    }
    if (code.length() > MAX_CODE_LENGTH) {
      throw new IllegalArgumentException(
          "Code must be " + MAX_CODE_LENGTH + " characters or less");
    }
  }

  /**
   * Validates the message content.
   *
   * @param content the content to validate
   * @throws IllegalArgumentException if content is invalid
   */
  private static void validateContent(String content) {
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Content cannot be null or blank");
    }
    if (content.length() > MAX_CONTENT_LENGTH) {
      throw new IllegalArgumentException(
          "Content must be " + MAX_CONTENT_LENGTH + " characters or less");
    }
  }
}
