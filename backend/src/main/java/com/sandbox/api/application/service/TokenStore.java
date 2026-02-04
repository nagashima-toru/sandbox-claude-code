package com.sandbox.api.application.service;

/**
 * Interface for managing refresh tokens.
 *
 * <p>This interface abstracts refresh token storage from the infrastructure layer, following Clean
 * Architecture principles.
 */
public interface TokenStore {

  /**
   * Stores a refresh token associated with a user ID.
   *
   * @param token the refresh token
   * @param userId the user ID
   */
  void store(String token, Long userId);

  /**
   * Validates if a refresh token exists in the store.
   *
   * @param token the refresh token to validate
   * @return true if the token exists, false otherwise
   */
  boolean isValid(String token);

  /**
   * Retrieves the user ID associated with a refresh token.
   *
   * @param token the refresh token
   * @return the user ID if found, null otherwise
   */
  Long getUserId(String token);

  /**
   * Removes a refresh token from the store.
   *
   * @param token the refresh token to remove
   */
  void remove(String token);

  /**
   * Removes all refresh tokens associated with a user ID.
   *
   * @param userId the user ID
   */
  void removeAllForUser(Long userId);
}
