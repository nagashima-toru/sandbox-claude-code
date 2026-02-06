package com.sandbox.api.application.usecase.auth;

import com.sandbox.api.application.dto.LoginResponse;
import com.sandbox.api.application.service.TokenProvider;
import com.sandbox.api.application.service.TokenStore;
import com.sandbox.api.domain.model.User;
import com.sandbox.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Use case for user login. */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;
  private final TokenStore tokenStore;

  /**
   * Authenticates a user and generates access and refresh tokens.
   *
   * @param username the username
   * @param password the password
   * @return LoginResponse containing access and refresh tokens
   * @throws BadCredentialsException if authentication fails
   */
  public LoginResponse execute(String username, String password) {
    // Sanitize username for logging to prevent log injection attacks
    String sanitizedUsername = sanitizeForLogging(username);
    log.debug("Login attempt for username: {}", sanitizedUsername);

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      log.warn("Failed login attempt for username: {}", sanitizedUsername);
      throw new BadCredentialsException("Invalid username or password");
    }

    String accessToken =
        tokenProvider.generateAccessToken(user.getUsername(), user.getRole().name());
    String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

    tokenStore.store(refreshToken, user.getId());

    log.info("Successful login for username: {}", sanitizedUsername);
    return new LoginResponse(accessToken, refreshToken, 3600);
  }

  /**
   * Sanitizes a string for safe logging by allowing only safe characters.
   *
   * <p>This method prevents log injection attacks by removing all characters except alphanumerics,
   * hyphens, underscores, and dots. Any potentially dangerous characters (control characters,
   * newlines, special symbols) are stripped out.
   *
   * @param input the input string
   * @return sanitized string safe for logging, containing only [a-zA-Z0-9._-]
   */
  private String sanitizeForLogging(String input) {
    if (input == null) {
      return "null";
    }
    // Only allow alphanumerics, dots, hyphens, and underscores to prevent log injection
    // This is a whitelist approach which is more secure than blacklisting specific characters
    return input.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
