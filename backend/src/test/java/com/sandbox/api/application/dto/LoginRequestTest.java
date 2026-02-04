package com.sandbox.api.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void constructor_withValidData_createsInstance() {
    LoginRequest request = new LoginRequest("testuser", "password123");

    assertThat(request.getUsername()).isEqualTo("testuser");
    assertThat(request.getPassword()).isEqualTo("password123");
  }

  @Test
  void validation_withValidData_passesValidation() {
    LoginRequest request = new LoginRequest("testuser", "password123");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  void validation_withBlankUsername_failsValidation() {
    LoginRequest request = new LoginRequest("", "password123");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
    assertThat(violations).anyMatch(v -> v.getMessage().equals("Username is required"));
  }

  @Test
  void validation_withNullUsername_failsValidation() {
    LoginRequest request = new LoginRequest(null, "password123");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Username is required");
  }

  @Test
  void validation_withShortUsername_failsValidation() {
    LoginRequest request = new LoginRequest("ab", "password123");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .isEqualTo("Username must be between 3 and 50 characters");
  }

  @Test
  void validation_withLongUsername_failsValidation() {
    String longUsername = "a".repeat(51);
    LoginRequest request = new LoginRequest(longUsername, "password123");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .isEqualTo("Username must be between 3 and 50 characters");
  }

  @Test
  void validation_withBlankPassword_failsValidation() {
    LoginRequest request = new LoginRequest("testuser", "");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
    assertThat(violations).anyMatch(v -> v.getMessage().equals("Password is required"));
  }

  @Test
  void validation_withNullPassword_failsValidation() {
    LoginRequest request = new LoginRequest("testuser", null);

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Password is required");
  }

  @Test
  void validation_withShortPassword_failsValidation() {
    LoginRequest request = new LoginRequest("testuser", "pass123");

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .isEqualTo("Password must be between 8 and 100 characters");
  }

  @Test
  void validation_withLongPassword_failsValidation() {
    String longPassword = "a".repeat(101);
    LoginRequest request = new LoginRequest("testuser", longPassword);

    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .isEqualTo("Password must be between 8 and 100 characters");
  }
}
