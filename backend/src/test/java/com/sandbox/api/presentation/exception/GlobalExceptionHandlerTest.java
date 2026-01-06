package com.sandbox.api.presentation.exception;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.presentation.dto.ErrorResponse;
import com.sandbox.api.presentation.dto.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/messages");
    }

    @Test
    void handleMessageNotFound_returns404WithErrorResponse() {
        // Arrange
        MessageNotFoundException exception = new MessageNotFoundException(1L);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMessageNotFound(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Message with id 1 not found");
        assertThat(response.getBody().getPath()).isEqualTo("/api/messages");
    }

    @Test
    void handleDuplicateCode_returns409WithErrorResponse() {
        // Arrange
        DuplicateMessageCodeException exception = new DuplicateMessageCodeException("duplicate");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateCode(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Message with code 'duplicate' already exists");
        assertThat(response.getBody().getPath()).isEqualTo("/api/messages");
    }

    @Test
    void handleValidationErrors_returns400WithValidationErrorResponse() {
        // Arrange
        FieldError fieldError = new FieldError("messageRequest", "code", "Code is required");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        // Act
        ResponseEntity<ValidationErrorResponse> response = exceptionHandler.handleValidationErrors(validationException, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getPath()).isEqualTo("/api/messages");
        assertThat(response.getBody().getErrors()).hasSize(1);
        assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("code");
        assertThat(response.getBody().getErrors().get(0).getMessage()).isEqualTo("Code is required");
    }
}
