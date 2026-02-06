package com.sandbox.api.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RefreshRequestTest {

  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void constructor_withValidData_createsInstance() {
    RefreshRequest request = new RefreshRequest("refresh-token-123");

    assertThat(request.getRefreshToken()).isEqualTo("refresh-token-123");
  }

  @Test
  void validation_withValidData_passesValidation() {
    RefreshRequest request = new RefreshRequest("refresh-token-123");

    Set<ConstraintViolation<RefreshRequest>> violations = validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  void validation_withBlankRefreshToken_failsValidation() {
    RefreshRequest request = new RefreshRequest("");

    Set<ConstraintViolation<RefreshRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Refresh token is required");
  }

  @Test
  void validation_withNullRefreshToken_failsValidation() {
    RefreshRequest request = new RefreshRequest(null);

    Set<ConstraintViolation<RefreshRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Refresh token is required");
  }
}
