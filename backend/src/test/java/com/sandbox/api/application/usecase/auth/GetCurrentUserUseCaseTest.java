package com.sandbox.api.application.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.sandbox.api.application.dto.UserResponse;
import org.junit.jupiter.api.Test;

class GetCurrentUserUseCaseTest {

  @Test
  void execute_shouldReturnMockUserResponse() {
    // Arrange
    GetCurrentUserUseCase useCase = new GetCurrentUserUseCase();

    // Act
    UserResponse response = useCase.execute();

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.username()).isEqualTo("testuser");
    assertThat(response.role()).isEqualTo("ADMIN");
  }
}
