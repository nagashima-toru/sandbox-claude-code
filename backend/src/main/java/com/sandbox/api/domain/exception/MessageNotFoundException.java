package com.sandbox.api.domain.exception;

/** Exception thrown when a message is not found. */
public class MessageNotFoundException extends RuntimeException {
  public MessageNotFoundException(Long id) {
    super("Message with id " + id + " not found");
  }
}
