package com.sandbox.api.presentation.dto;

import com.sandbox.api.domain.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private String code;
    private String content;

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getCode(),
                message.getContent()
        );
    }
}
