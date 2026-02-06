package com.sandbox.api.application.usecase.auth;

import com.sandbox.api.application.dto.LoginResponse;
import com.sandbox.api.application.service.TokenProvider;
import com.sandbox.api.application.service.TokenStore;
import com.sandbox.api.domain.model.User;
import com.sandbox.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/** Use case for refreshing access token. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase {

  private final TokenProvider tokenProvider;
  private final TokenStore tokenStore;
  private final UserRepository userRepository;

  /**
   * Refreshes the access token using a valid refresh token.
   *
   * @param refreshToken the refresh token
   * @return LoginResponse containing new access token and the same refresh token
   * @throws BadCredentialsException if the refresh token is invalid
   */
  public LoginResponse execute(String refreshToken) {
    log.debug("Token refresh attempt");

    if (!tokenProvider.validateToken(refreshToken)) {
      log.warn("Invalid refresh token");
      throw new BadCredentialsException("Invalid refresh token");
    }

    if (!tokenStore.isValid(refreshToken)) {
      log.warn("Refresh token not found in store");
      throw new BadCredentialsException("Invalid refresh token");
    }

    Long userId = tokenStore.getUserId(refreshToken);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));

    String newAccessToken =
        tokenProvider.generateAccessToken(user.getUsername(), user.getRole().name());

    log.info("Token refreshed for user ID: {}", userId);
    return new LoginResponse(newAccessToken, refreshToken, 3600);
  }
}
