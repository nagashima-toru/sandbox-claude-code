package com.sandbox.api.domain.repository;

import com.sandbox.api.domain.model.User;
import java.util.Optional;

/** Repository interface for managing users. */
public interface UserRepository {
  /**
   * Finds a user by username.
   *
   * @param username the username to search for
   * @return an Optional containing the user if found, or empty if not found
   */
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by ID.
   *
   * @param id the user ID to search for
   * @return an Optional containing the user if found, or empty if not found
   */
  Optional<User> findById(Long id);

  /**
   * Checks if a user with the given username exists.
   *
   * @param username the username to check
   * @return true if a user with the given username exists, false otherwise
   */
  boolean existsByUsername(String username);
}
