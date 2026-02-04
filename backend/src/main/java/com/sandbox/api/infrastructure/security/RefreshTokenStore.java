package com.sandbox.api.infrastructure.security;

import com.sandbox.api.application.service.TokenStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * In-memory store for managing refresh tokens.
 *
 * <p>This implementation uses a ConcurrentHashMap to store refresh tokens. In production, consider
 * using Redis or a database for persistence and scalability.
 */
@Component
@Slf4j
public class RefreshTokenStore implements TokenStore {

  private final Map<String, Long> tokenToUserIdMap = new ConcurrentHashMap<>();

  /**
   * Stores a refresh token associated with a user ID.
   *
   * @param token the refresh token
   * @param userId the user ID
   */
  public void store(String token, Long userId) {
    tokenToUserIdMap.put(token, userId);
    log.debug("Stored refresh token for user ID: {}", userId);
  }

  /**
   * Validates if a refresh token exists in the store.
   *
   * @param token the refresh token to validate
   * @return true if the token exists, false otherwise
   */
  public boolean isValid(String token) {
    return tokenToUserIdMap.containsKey(token);
  }

  /**
   * Retrieves the user ID associated with a refresh token.
   *
   * @param token the refresh token
   * @return the user ID if found, null otherwise
   */
  public Long getUserId(String token) {
    return tokenToUserIdMap.get(token);
  }

  /**
   * Removes a refresh token from the store (for logout).
   *
   * @param token the refresh token to remove
   */
  public void remove(String token) {
    Long userId = tokenToUserIdMap.remove(token);
    if (userId != null) {
      log.debug("Removed refresh token for user ID: {}", userId);
    }
  }

  /**
   * Removes all refresh tokens associated with a user ID.
   *
   * @param userId the user ID
   */
  public void removeAllForUser(Long userId) {
    tokenToUserIdMap.entrySet().removeIf(entry -> entry.getValue().equals(userId));
    log.debug("Removed all refresh tokens for user ID: {}", userId);
  }
}
