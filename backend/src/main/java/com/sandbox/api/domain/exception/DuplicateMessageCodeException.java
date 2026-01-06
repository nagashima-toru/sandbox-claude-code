package com.sandbox.api.domain.exception;

public class DuplicateMessageCodeException extends RuntimeException {
    public DuplicateMessageCodeException(String code) {
        super("Message with code '" + code + "' already exists");
    }
}
