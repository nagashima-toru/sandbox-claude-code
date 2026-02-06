package com.sandbox.api.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Immutable domain model representing a user.
 *
 * <p>This class follows Clean Architecture principles by ensuring immutability and encapsulating
 * domain logic within the domain layer.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class User {
  private final Long id;
  private final String username;
  private final String passwordHash;
  private final Role role;
  private final boolean enabled;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  private static final int MAX_USERNAME_LENGTH = 50;

  /**
   * Creates a new user with the given username, password hash, and role.
   *
   * @param username the unique username
   * @param passwordHash the BCrypt hashed password
   * @param role the user role
   * @return a new User instance
   * @throws IllegalArgumentException if username or passwordHash is null or blank, or username
   *     exceeds maximum length
   */
  public static User createNew(String username, String passwordHash, Role role) {
    validateUsername(username);
    validatePasswordHash(passwordHash);
    validateRole(role);

    return User.builder()
        .username(username)
        .passwordHash(passwordHash)
        .role(role)
        .enabled(true)
        .createdAt(LocalDateTime.now())
        .build();
  }

  /**
   * Creates a new User instance with updated password.
   *
   * @param newPasswordHash the new BCrypt hashed password
   * @return a new User instance with updated password
   * @throws IllegalArgumentException if newPasswordHash is invalid
   */
  public User updatePassword(String newPasswordHash) {
    validatePasswordHash(newPasswordHash);

    return this.toBuilder().passwordHash(newPasswordHash).updatedAt(LocalDateTime.now()).build();
  }

  /**
   * Creates a new User instance with updated role.
   *
   * @param newRole the new role
   * @return a new User instance with updated role
   * @throws IllegalArgumentException if newRole is invalid
   */
  public User updateRole(Role newRole) {
    validateRole(newRole);

    return this.toBuilder().role(newRole).updatedAt(LocalDateTime.now()).build();
  }

  /**
   * Creates a new User instance with the enabled flag toggled.
   *
   * @param isEnabled the new enabled status
   * @return a new User instance with updated enabled status
   */
  public User updateEnabled(boolean isEnabled) {
    return this.toBuilder().enabled(isEnabled).updatedAt(LocalDateTime.now()).build();
  }

  /**
   * Creates a new User instance with the specified ID.
   *
   * @param newId the new ID
   * @return a new User with the specified ID
   */
  public User withId(Long newId) {
    return this.toBuilder().id(newId).build();
  }

  /**
   * Validates the username.
   *
   * @param username the username to validate
   * @throws IllegalArgumentException if username is invalid
   */
  private static void validateUsername(String username) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Username cannot be null or blank");
    }
    if (username.length() > MAX_USERNAME_LENGTH) {
      throw new IllegalArgumentException(
          "Username must be " + MAX_USERNAME_LENGTH + " characters or less");
    }
  }

  /**
   * Validates the password hash.
   *
   * @param passwordHash the password hash to validate
   * @throws IllegalArgumentException if passwordHash is invalid
   */
  private static void validatePasswordHash(String passwordHash) {
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("Password hash cannot be null or blank");
    }
  }

  /**
   * Validates the role.
   *
   * @param role the role to validate
   * @throws IllegalArgumentException if role is invalid
   */
  private static void validateRole(Role role) {
    if (role == null) {
      throw new IllegalArgumentException("Role cannot be null");
    }
  }
}
