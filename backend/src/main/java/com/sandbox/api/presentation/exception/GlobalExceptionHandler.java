package com.sandbox.api.presentation.exception;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.presentation.dto.ErrorResponse;
import com.sandbox.api.presentation.dto.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Global exception handler for REST API. */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles MessageNotFoundException and returns a 404 response.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return response entity with error details
   */
  @ExceptionHandler(MessageNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotFound(
      MessageNotFoundException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handles DuplicateMessageCodeException and returns a 409 response.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return response entity with error details
   */
  @ExceptionHandler(DuplicateMessageCodeException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateCode(
      DuplicateMessageCodeException ex, HttpServletRequest request) {
    ErrorResponse error =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handles validation errors and returns a 400 response with field-specific error details.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return response entity with validation error details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ValidationErrorResponse.FieldError> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    new ValidationErrorResponse.FieldError(
                        error.getField(), error.getDefaultMessage()))
            .collect(Collectors.toList());

    ValidationErrorResponse error =
        new ValidationErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed",
            request.getRequestURI(),
            fieldErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }
}
