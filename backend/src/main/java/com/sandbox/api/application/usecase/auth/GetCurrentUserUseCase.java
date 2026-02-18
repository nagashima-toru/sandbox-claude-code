package com.sandbox.api.application.usecase.auth;

import com.sandbox.api.application.dto.UserResponse;
import com.sandbox.api.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving current user information.
 *
 * <p>This use case retrieves the username and role of the currently authenticated user from the
 * Spring Security context.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetCurrentUserUseCase {

  /**
   * Retrieves the current user's information from the security context.
   *
   * @return UserResponse containing username and role
   * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
   *     if the user is not authenticated
   */
  public UserResponse execute() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      throw new org.springframework.security.authentication
          .AuthenticationCredentialsNotFoundException("User is not authenticated");
    }

    String username = authentication.getName();

    // Extract role from authorities
    String roleName =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "User has no role assigned: " + sanitizeForLogging(username)));

    Role role;
    try {
      role = Role.valueOf(roleName);
    } catch (IllegalArgumentException e) {
      log.error("Invalid role: {} for user: {}", roleName, sanitizeForLogging(username));
      throw new IllegalStateException("Invalid role: " + roleName, e);
    }

    log.debug("Retrieved current user: {} with role: {}", sanitizeForLogging(username), role);
    return new UserResponse(username, role);
  }

  /**
   * Sanitizes a string for safe logging by allowing only safe characters.
   *
   * <p>This method prevents log injection attacks by removing all characters except alphanumerics,
   * hyphens, underscores, and dots.
   *
   * @param input the input string
   * @return sanitized string safe for logging
   */
  private String sanitizeForLogging(String input) {
    if (input == null) {
      return "null";
    }
    return input.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
