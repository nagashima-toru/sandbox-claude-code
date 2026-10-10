package com.sandbox.api.presentation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.sandbox.api.domain.model.Role;
import org.junit.jupiter.api.Test;

class AuthMapperTest {

  @Test
  void toInternal_convertsGeneratedLoginRequestToInternal() {
    // Arrange
    com.sandbox.api.presentation.generated.model.LoginRequest generated =
        new com.sandbox.api.presentation.generated.model.LoginRequest();
    generated.setUsername("testuser");
    generated.setPassword("password123");

    // Act
    com.sandbox.api.application.dto.LoginRequest internal = AuthMapper.toInternal(generated);

    // Assert
    assertThat(internal.getUsername()).isEqualTo("testuser");
    assertThat(internal.getPassword()).isEqualTo("password123");
  }

  @Test
  void toGenerated_convertsInternalLoginResponseToGenerated() {
    // Arrange
    com.sandbox.api.application.dto.LoginResponse internal =
        new com.sandbox.api.application.dto.LoginResponse("access-token", "refresh-token", 3600);

    // Act
    com.sandbox.api.presentation.generated.model.LoginResponse generated =
        AuthMapper.toGenerated(internal);

    // Assert
    assertThat(generated.getAccessToken()).isEqualTo("access-token");
    assertThat(generated.getRefreshToken()).isEqualTo("refresh-token");
    assertThat(generated.getTokenType()).isEqualTo("Bearer");
    assertThat(generated.getExpiresIn()).isEqualTo(3600);
  }

  @Test
  void toInternal_convertsGeneratedRefreshRequestToInternal() {
    // Arrange
    com.sandbox.api.presentation.generated.model.RefreshRequest generated =
        new com.sandbox.api.presentation.generated.model.RefreshRequest();
    generated.setRefreshToken("refresh-token-123");

    // Act
    com.sandbox.api.application.dto.RefreshRequest internal = AuthMapper.toInternal(generated);

    // Assert
    assertThat(internal.getRefreshToken()).isEqualTo("refresh-token-123");
  }

  @Test
  void toGenerated_shouldConvertUserResponse() {
    // Arrange
    com.sandbox.api.application.dto.UserResponse internal =
        new com.sandbox.api.application.dto.UserResponse("testuser", Role.ADMIN);

    // Act
    var generated = AuthMapper.toGenerated(internal);

    // Assert
    assertThat(generated.getUsername()).isEqualTo("testuser");
    assertThat(generated.getRole())
        .isEqualTo(com.sandbox.api.presentation.generated.model.UserResponse.RoleEnum.ADMIN);
  }
}
