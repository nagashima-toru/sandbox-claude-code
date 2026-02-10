package com.sandbox.api.application.dto;

import com.sandbox.api.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for current user information.
 *
 * <p>This DTO contains the username and role of the currently authenticated user, typically
 * returned from the GET /api/users/me endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  /** The username of the current user. */
  private String username;

  /** The role of the current user (ADMIN or VIEWER). */
  private Role role;
}
