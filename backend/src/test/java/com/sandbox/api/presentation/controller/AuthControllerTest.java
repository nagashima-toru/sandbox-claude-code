package com.sandbox.api.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sandbox.api.application.usecase.auth.LoginUseCase;
import com.sandbox.api.application.usecase.auth.LogoutUseCase;
import com.sandbox.api.application.usecase.auth.RefreshTokenUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private LoginUseCase loginUseCase;
  @Mock private RefreshTokenUseCase refreshTokenUseCase;
  @Mock private LogoutUseCase logoutUseCase;

  @InjectMocks private AuthController authController;

  @Test
  void login_withValidCredentials_returnsOkWithTokens() {
    // Arrange
    com.sandbox.api.presentation.generated.model.LoginRequest request =
        new com.sandbox.api.presentation.generated.model.LoginRequest();
    request.setUsername("testuser");
    request.setPassword("password123");

    com.sandbox.api.application.dto.LoginResponse useCaseResponse =
        new com.sandbox.api.application.dto.LoginResponse("access-token", "refresh-token", 3600);
    when(loginUseCase.execute("testuser", "password123")).thenReturn(useCaseResponse);

    // Act
    ResponseEntity<com.sandbox.api.presentation.generated.model.LoginResponse> response =
        authController.login(request);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");
    assertThat(response.getBody().getRefreshToken()).isEqualTo("refresh-token");
    assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
    assertThat(response.getBody().getExpiresIn()).isEqualTo(3600);

    verify(loginUseCase).execute("testuser", "password123");
  }

  @Test
  void refreshToken_withValidRefreshToken_returnsOkWithNewTokens() {
    // Arrange
    com.sandbox.api.presentation.generated.model.RefreshRequest request =
        new com.sandbox.api.presentation.generated.model.RefreshRequest();
    request.setRefreshToken("refresh-token-123");

    com.sandbox.api.application.dto.LoginResponse useCaseResponse =
        new com.sandbox.api.application.dto.LoginResponse(
            "new-access-token", "refresh-token-123", 3600);
    when(refreshTokenUseCase.execute("refresh-token-123")).thenReturn(useCaseResponse);

    // Act
    ResponseEntity<com.sandbox.api.presentation.generated.model.LoginResponse> response =
        authController.refreshToken(request);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getAccessToken()).isEqualTo("new-access-token");
    assertThat(response.getBody().getRefreshToken()).isEqualTo("refresh-token-123");

    verify(refreshTokenUseCase).execute("refresh-token-123");
  }

  @Test
  void logout_withRefreshToken_returnsNoContent() {
    // Arrange
    com.sandbox.api.presentation.generated.model.RefreshRequest request =
        new com.sandbox.api.presentation.generated.model.RefreshRequest();
    request.setRefreshToken("refresh-token-to-invalidate");

    // Act
    ResponseEntity<Void> response = authController.logout(request);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(logoutUseCase).execute("refresh-token-to-invalidate");
  }
}
