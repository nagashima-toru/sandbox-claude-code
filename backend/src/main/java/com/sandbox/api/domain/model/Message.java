package com.sandbox.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Domain model representing a message. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Message {
  private Long id;
  private String code;
  private String content;

  /**
   * Creates a new message with the given code and content.
   *
   * @param code the unique code for the message
   * @param content the content of the message
   * @return a new Message instance
   * @throws IllegalArgumentException if code or content is null or blank
   */
  public static Message createNew(String code, String content) {
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("Code cannot be null or blank");
    }
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Content cannot be null or blank");
    }
    return Message.builder().code(code).content(content).build();
  }

  public Message withId(Long newId) {
    return Message.builder().id(newId).code(this.code).content(this.content).build();
  }
}
