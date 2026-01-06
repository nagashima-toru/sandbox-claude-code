package com.sandbox.api.presentation.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidationErrorResponse extends ErrorResponse {

    private List<FieldError> errors;

    public ValidationErrorResponse() {
        super();
        this.errors = new ArrayList<>();
    }

    public ValidationErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, List<FieldError> errors) {
        super(timestamp, status, error, message, path);
        this.errors = errors;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }

    public static class FieldError {
        private String field;
        private String message;

        public FieldError() {
        }

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
