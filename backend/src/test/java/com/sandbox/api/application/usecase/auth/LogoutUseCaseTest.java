package com.sandbox.api.application.usecase.auth;

import static org.mockito.Mockito.verify;

import com.sandbox.api.application.service.TokenStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

  @Mock private TokenStore tokenStore;

  @InjectMocks private LogoutUseCase logoutUseCase;

  @Test
  void execute_removesRefreshToken() {
    // Act
    logoutUseCase.execute("refresh-token");

    // Assert
    verify(tokenStore).remove("refresh-token");
  }

  @Test
  void execute_doesNotThrowExceptionForNonexistentToken() {
    // This should not throw any exception
    logoutUseCase.execute("nonexistent-token");

    verify(tokenStore).remove("nonexistent-token");
  }
}
