package com.sandbox.api.application.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sandbox.api.application.dto.LoginResponse;
import com.sandbox.api.application.service.TokenProvider;
import com.sandbox.api.application.service.TokenStore;
import com.sandbox.api.domain.model.Role;
import com.sandbox.api.domain.model.User;
import com.sandbox.api.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

  @Mock private TokenProvider tokenProvider;
  @Mock private TokenStore tokenStore;
  @Mock private UserRepository userRepository;

  @InjectMocks private RefreshTokenUseCase refreshTokenUseCase;

  @Test
  void execute_withValidRefreshToken_returnsNewAccessToken() {
    // Arrange
    User user = User.builder().id(1L).username("testuser").role(Role.ADMIN).build();

    when(tokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
    when(tokenStore.isValid("valid-refresh-token")).thenReturn(true);
    when(tokenStore.getUserId("valid-refresh-token")).thenReturn(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(tokenProvider.generateAccessToken(anyString(), anyString()))
        .thenReturn("new-access-token");

    // Act
    LoginResponse response = refreshTokenUseCase.execute("valid-refresh-token");

    // Assert
    assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
    assertThat(response.getTokenType()).isEqualTo("Bearer");
    assertThat(response.getExpiresIn()).isEqualTo(3600);

    verify(tokenProvider).generateAccessToken("testuser", "ADMIN");
  }

  @Test
  void execute_withInvalidToken_throwsBadCredentialsException() {
    // Arrange
    when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> refreshTokenUseCase.execute("invalid-token"))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid refresh token");
  }

  @Test
  void execute_withTokenNotInStore_throwsBadCredentialsException() {
    // Arrange
    when(tokenProvider.validateToken("valid-token")).thenReturn(true);
    when(tokenStore.isValid("valid-token")).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> refreshTokenUseCase.execute("valid-token"))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid refresh token");
  }

  @Test
  void execute_withNonexistentUser_throwsBadCredentialsException() {
    // Arrange
    when(tokenProvider.validateToken("valid-token")).thenReturn(true);
    when(tokenStore.isValid("valid-token")).thenReturn(true);
    when(tokenStore.getUserId("valid-token")).thenReturn(999L);
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> refreshTokenUseCase.execute("valid-token"))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("User not found");
  }
}
