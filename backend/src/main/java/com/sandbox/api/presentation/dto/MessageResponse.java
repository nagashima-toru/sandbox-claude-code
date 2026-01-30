package com.sandbox.api.presentation.dto;

import com.sandbox.api.domain.model.Message;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO representing a message. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

  private Long id;
  private String code;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Creates a MessageResponse from a Message domain model.
   *
   * @param message the domain model
   * @return the response DTO
   */
  public static MessageResponse from(Message message) {
    return new MessageResponse(
        message.getId(),
        message.getCode(),
        message.getContent(),
        message.getCreatedAt(),
        message.getUpdatedAt());
  }
}
