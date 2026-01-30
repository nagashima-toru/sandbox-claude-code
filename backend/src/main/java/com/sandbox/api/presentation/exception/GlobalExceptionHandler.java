package com.sandbox.api.presentation.exception;

import com.sandbox.api.domain.exception.DuplicateMessageCodeException;
import com.sandbox.api.domain.exception.MessageNotFoundException;
import com.sandbox.api.presentation.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Global exception handler for REST API with RFC 7807 Problem Details support. */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final String ERROR_TYPE_BASE_URI = "https://api.example.com/errors";
  private static final MediaType PROBLEM_JSON = MediaType.parseMediaType("application/problem+json");

  /**
   * Handles MessageNotFoundException and returns a 404 response in RFC 7807 format.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return response entity with RFC 7807 error details
   */
  @ExceptionHandler(MessageNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotFound(
      MessageNotFoundException ex, HttpServletRequest request) {
    ErrorResponse error =
        ErrorResponse.builder()
            .type(ERROR_TYPE_BASE_URI + "/not-found")
            .title("Not Found")
            .status(HttpStatus.NOT_FOUND.value())
            .detail(ex.getMessage())
            .instance(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(PROBLEM_JSON)
        .body(error);
  }

  /**
   * Handles DuplicateMessageCodeException and returns a 409 response in RFC 7807 format.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return response entity with RFC 7807 error details
   */
  @ExceptionHandler(DuplicateMessageCodeException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateCode(
      DuplicateMessageCodeException ex, HttpServletRequest request) {
    ErrorResponse error =
        ErrorResponse.builder()
            .type(ERROR_TYPE_BASE_URI + "/duplicate-code")
            .title("Duplicate Code")
            .status(HttpStatus.CONFLICT.value())
            .detail(ex.getMessage())
            .instance(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .contentType(PROBLEM_JSON)
        .body(error);
  }

  /**
   * Handles validation errors and returns a 400 response in RFC 7807 format with field-specific error details.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return response entity with RFC 7807 validation error details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ErrorResponse.ValidationError> validationErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    new ErrorResponse.ValidationError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()))
            .collect(Collectors.toList());

    ErrorResponse error =
        ErrorResponse.builder()
            .type(ERROR_TYPE_BASE_URI + "/validation-error")
            .title("Validation Error")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("The request body contains invalid fields.")
            .instance(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .errors(validationErrors)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(PROBLEM_JSON)
        .body(error);
  }

  /**
   * Handles general exceptions and returns a 500 response in RFC 7807 format.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return response entity with RFC 7807 error details
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(
      Exception ex, HttpServletRequest request) {
    LOGGER.error("Unhandled exception occurred during request processing for URI: {}", request.getRequestURI(), ex);
    ErrorResponse error =
        ErrorResponse.builder()
            .type(ERROR_TYPE_BASE_URI + "/internal-error")
            .title("Internal Server Error")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .detail("An unexpected error occurred. Please try again later.")
            .instance(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(PROBLEM_JSON)
        .body(error);
  }
}
