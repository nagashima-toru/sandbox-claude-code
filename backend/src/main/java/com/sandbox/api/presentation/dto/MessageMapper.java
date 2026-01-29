package com.sandbox.api.presentation.dto;

import com.sandbox.api.domain.model.Message;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.data.domain.Page;

/** Mapper between generated OpenAPI DTOs and internal DTOs. */
public class MessageMapper {

  private MessageMapper() {
    // Utility class
  }

  /**
   * Converts generated MessageRequest to internal MessageRequest.
   *
   * @param generated the generated MessageRequest
   * @return internal MessageRequest
   */
  public static MessageRequest toInternal(
      com.sandbox.api.presentation.generated.model.MessageRequest generated) {
    return new MessageRequest(generated.getCode(), generated.getContent());
  }

  /**
   * Converts internal MessageResponse to generated MessageResponse.
   *
   * @param internal the internal MessageResponse
   * @return generated MessageResponse
   */
  public static com.sandbox.api.presentation.generated.model.MessageResponse toGenerated(
      MessageResponse internal) {
    var generated = new com.sandbox.api.presentation.generated.model.MessageResponse();
    generated.setId(internal.getId());
    generated.setCode(internal.getCode());
    generated.setContent(internal.getContent());
    generated.setCreatedAt(toOffsetDateTime(internal.getCreatedAt()));
    generated.setUpdatedAt(toOffsetDateTime(internal.getUpdatedAt()));
    return generated;
  }

  /**
   * Converts a Page of Message to generated MessagePage.
   *
   * @param page the page of messages
   * @return generated MessagePage
   */
  public static com.sandbox.api.presentation.generated.model.MessagePage toMessagePage(
      Page<Message> page) {
    var messagePage = new com.sandbox.api.presentation.generated.model.MessagePage();

    // Convert messages
    List<com.sandbox.api.presentation.generated.model.MessageResponse> messages =
        page.getContent().stream()
            .map(MessageResponse::from)
            .map(MessageMapper::toGenerated)
            .toList();
    messagePage.setContent(messages);

    // Set page info
    var pageInfo = new com.sandbox.api.presentation.generated.model.MessagePagePage();
    pageInfo.setSize(page.getSize());
    pageInfo.setNumber(page.getNumber());
    pageInfo.setTotalElements(page.getTotalElements());
    pageInfo.setTotalPages(page.getTotalPages());
    messagePage.setPage(pageInfo);

    return messagePage;
  }

  private static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }
}
