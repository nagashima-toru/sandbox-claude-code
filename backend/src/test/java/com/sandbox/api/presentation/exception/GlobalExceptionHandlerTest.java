package com.sandbox.api.presentation.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.presentation.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
  @Mock private HttpServletRequest request;
  @Mock private MethodArgumentNotValidException validationException;
  @Mock private BindingResult bindingResult;
  @InjectMocks private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    when(request.getRequestURI()).thenReturn("/api/messages");
  }

  @Test
  void handleMessageNotFound_returns404WithErrorResponse() {
    // Arrange
    MessageNotFoundException exception = new MessageNotFoundException(1L);
    // Act
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleMessageNotFound(exception, request);
    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(404);
    assertThat(response.getBody().getTitle()).isEqualTo("Not Found");
    assertThat(response.getBody().getDetail()).isEqualTo("Message with id 1 not found");
    assertThat(response.getBody().getInstance()).isEqualTo("/api/messages");
    assertThat(response.getBody().getType()).contains("/not-found");
  }

  @Test
  void handleDuplicateCode_returns409WithErrorResponse() {
    DuplicateMessageCodeException exception = new DuplicateMessageCodeException("duplicate");
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleDuplicateCode(exception, request);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(409);
    assertThat(response.getBody().getTitle()).isEqualTo("Duplicate Code");
    assertThat(response.getBody().getDetail())
        .isEqualTo("Message with code 'duplicate' already exists");
    assertThat(response.getBody().getType()).contains("/duplicate-code");
  }

  @Test
  void handleValidationErrors_returns400WithValidationErrorResponse() {
    FieldError fieldError =
        new FieldError("messageRequest", "code", "invalid", false, null, null, "Code is required");
    when(validationException.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleValidationErrors(validationException, request);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(400);
    assertThat(response.getBody().getTitle()).isEqualTo("Validation Error");
    assertThat(response.getBody().getDetail())
        .isEqualTo("The request body contains invalid fields.");
    assertThat(response.getBody().getType()).contains("/validation-error");
    assertThat(response.getBody().getErrors()).hasSize(1);
    assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("code");
    assertThat(response.getBody().getErrors().get(0).getMessage()).isEqualTo("Code is required");
    assertThat(response.getBody().getErrors().get(0).getRejectedValue()).isEqualTo("invalid");
  }

  @Test
  void handleIllegalArgument_returns400WithErrorResponse() {
    // Arrange
    IllegalArgumentException exception = new IllegalArgumentException("Invalid sort field: malicious");

    // Act
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleIllegalArgument(exception, request);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(400);
    assertThat(response.getBody().getTitle()).isEqualTo("Invalid Argument");
    assertThat(response.getBody().getDetail()).isEqualTo("Invalid sort field: malicious");
    assertThat(response.getBody().getType()).contains("/invalid-argument");
    assertThat(response.getBody().getInstance()).isEqualTo("/api/messages");
  }

  @Test
  void handleGeneralException_returns500WithErrorResponse() {
    Exception exception = new RuntimeException("Unexpected error");
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleGeneralException(exception, request);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(500);
    assertThat(response.getBody().getTitle()).isEqualTo("Internal Server Error");
    assertThat(response.getBody().getDetail())
        .isEqualTo("An unexpected error occurred. Please try again later.");
    assertThat(response.getBody().getType()).contains("/internal-error");
    assertThat(response.getBody().getInstance()).isEqualTo("/api/messages");
  }
}
