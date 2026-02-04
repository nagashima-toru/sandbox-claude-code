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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private TokenProvider tokenProvider;
  @Mock private TokenStore tokenStore;

  @InjectMocks private LoginUseCase loginUseCase;

  @Test
  void execute_withValidCredentials_returnsTokens() {
    // Arrange
    User user =
        User.builder()
            .id(1L)
            .username("testuser")
            .passwordHash("$2a$10$hashedPassword")
            .role(Role.ADMIN)
            .build();

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
    when(tokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
    when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");

    // Act
    LoginResponse response = loginUseCase.execute("testuser", "password123");

    // Assert
    assertThat(response.getAccessToken()).isEqualTo("access-token");
    assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    assertThat(response.getTokenType()).isEqualTo("Bearer");
    assertThat(response.getExpiresIn()).isEqualTo(3600);

    verify(tokenStore).store("refresh-token", 1L);
  }

  @Test
  void execute_withNonexistentUser_throwsBadCredentialsException() {
    // Arrange
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> loginUseCase.execute("nonexistent", "password123"))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid username or password");
  }

  @Test
  void execute_withInvalidPassword_throwsBadCredentialsException() {
    // Arrange
    User user =
        User.builder()
            .id(1L)
            .username("testuser")
            .passwordHash("$2a$10$hashedPassword")
            .role(Role.ADMIN)
            .build();

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongpassword", user.getPasswordHash())).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> loginUseCase.execute("testuser", "wrongpassword"))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid username or password");
  }

  @Test
  void execute_generatesTokensWithCorrectParameters() {
    // Arrange
    User user =
        User.builder()
            .id(1L)
            .username("testuser")
            .passwordHash("$2a$10$hashedPassword")
            .role(Role.ADMIN)
            .build();

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
    when(tokenProvider.generateAccessToken("testuser", "ADMIN")).thenReturn("access-token");
    when(tokenProvider.generateRefreshToken("testuser")).thenReturn("refresh-token");

    // Act
    loginUseCase.execute("testuser", "password123");

    // Assert
    verify(tokenProvider).generateAccessToken("testuser", "ADMIN");
    verify(tokenProvider).generateRefreshToken("testuser");
  }
}
