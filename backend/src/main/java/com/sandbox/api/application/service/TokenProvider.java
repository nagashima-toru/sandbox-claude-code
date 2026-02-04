package com.sandbox.api.application.service;

/**
 * Interface for JWT token generation and validation.
 *
 * <p>This interface abstracts token operations from the infrastructure layer, following Clean
 * Architecture principles.
 */
public interface TokenProvider {

  /**
   * Generates an access token for the given username and role.
   *
   * @param username the username
   * @param role the user role
   * @return JWT access token
   */
  String generateAccessToken(String username, String role);

  /**
   * Generates a refresh token for the given username.
   *
   * @param username the username
   * @return JWT refresh token
   */
  String generateRefreshToken(String username);

  /**
   * Validates the given token.
   *
   * @param token the token to validate
   * @return true if valid, false otherwise
   */
  boolean validateToken(String token);

  /**
   * Extracts the username from the token.
   *
   * @param token the token
   * @return the username
   */
  String getUsernameFromToken(String token);

  /**
   * Extracts the role from the token.
   *
   * @param token the token
   * @return the role
   */
  String getRoleFromToken(String token);
}
