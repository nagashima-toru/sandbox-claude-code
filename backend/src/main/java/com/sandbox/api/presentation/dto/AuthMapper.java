package com.sandbox.api.presentation.dto;

/**
 * Mapper for converting between internal authentication DTOs and OpenAPI generated models.
 *
 * <p>This mapper handles the conversion between application layer DTOs and presentation layer
 * generated models.
 */
public class AuthMapper {

  private AuthMapper() {
    // Utility class
  }

  /**
   * Converts OpenAPI generated LoginRequest to internal LoginRequest.
   *
   * @param generated the generated LoginRequest
   * @return internal LoginRequest
   */
  public static com.sandbox.api.application.dto.LoginRequest toInternal(
      com.sandbox.api.presentation.generated.model.LoginRequest generated) {
    return new com.sandbox.api.application.dto.LoginRequest(
        generated.getUsername(), generated.getPassword());
  }

  /**
   * Converts internal LoginResponse to OpenAPI generated LoginResponse.
   *
   * @param internal the internal LoginResponse
   * @return generated LoginResponse
   */
  public static com.sandbox.api.presentation.generated.model.LoginResponse toGenerated(
      com.sandbox.api.application.dto.LoginResponse internal) {
    com.sandbox.api.presentation.generated.model.LoginResponse generated =
        new com.sandbox.api.presentation.generated.model.LoginResponse();
    generated.setAccessToken(internal.getAccessToken());
    generated.setRefreshToken(internal.getRefreshToken());
    generated.setTokenType(internal.getTokenType());
    generated.setExpiresIn((int) internal.getExpiresIn());
    return generated;
  }

  /**
   * Converts OpenAPI generated RefreshRequest to internal RefreshRequest.
   *
   * @param generated the generated RefreshRequest
   * @return internal RefreshRequest
   */
  public static com.sandbox.api.application.dto.RefreshRequest toInternal(
      com.sandbox.api.presentation.generated.model.RefreshRequest generated) {
    return new com.sandbox.api.application.dto.RefreshRequest(generated.getRefreshToken());
  }
}
