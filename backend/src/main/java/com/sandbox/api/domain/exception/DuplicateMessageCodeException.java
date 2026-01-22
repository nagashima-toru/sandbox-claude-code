package com.sandbox.api.domain.exception;

/** Exception thrown when attempting to create a message with a duplicate code. */
public class DuplicateMessageCodeException extends RuntimeException {
  public DuplicateMessageCodeException(String code) {
    super("Message with code '" + code + "' already exists");
  }
}
