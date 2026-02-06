package com.sandbox.api.application.usecase.auth;

import com.sandbox.api.application.service.TokenStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Use case for user logout. */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutUseCase {

  private final TokenStore tokenStore;

  /**
   * Logs out a user by removing their refresh token from the store.
   *
   * @param refreshToken the refresh token to invalidate
   */
  public void execute(String refreshToken) {
    log.debug("Logout attempt");
    tokenStore.remove(refreshToken);
    log.info("User logged out successfully");
  }
}
