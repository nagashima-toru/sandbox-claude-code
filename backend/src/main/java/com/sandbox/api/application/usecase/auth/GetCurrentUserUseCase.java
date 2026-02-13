package com.sandbox.api.application.usecase.auth;

import com.sandbox.api.application.dto.UserResponse;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving current user information.
 *
 * <p>TEMPORARY IMPLEMENTATION: Returns mock data to fix build failure. This will be replaced with
 * actual authentication logic in Epic #133.
 */
@Service
public class GetCurrentUserUseCase {

  /**
   * Executes the use case to get current user information.
   *
   * @return user response with mock data
   */
  public UserResponse execute() {
    // Temporary implementation: return mock data
    return new UserResponse("testuser", "ADMIN");
  }
}
