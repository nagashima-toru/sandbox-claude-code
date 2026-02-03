package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** MyBatis mapper for user database operations. */
@Mapper
public interface UserMapper {
  /**
   * Finds a user by username.
   *
   * @param username the username to search for
   * @return the user if found, null otherwise
   */
  User findByUsername(@Param("username") String username);

  /**
   * Finds a user by ID.
   *
   * @param id the user ID to search for
   * @return the user if found, null otherwise
   */
  User findById(@Param("id") Long id);

  /**
   * Checks if a user with the given username exists.
   *
   * @param username the username to check
   * @return true if a user exists, false otherwise
   */
  boolean existsByUsername(@Param("username") String username);
}
