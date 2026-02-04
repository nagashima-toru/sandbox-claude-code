package com.sandbox.api.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for login and token refresh operations. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private String accessToken;
  private String refreshToken;
  private String tokenType = "Bearer";
  private long expiresIn;

  /**
   * Creates a LoginResponse with Bearer token type.
   *
   * @param accessToken the JWT access token
   * @param refreshToken the JWT refresh token
   * @param expiresIn the access token expiration time in seconds
   */
  public LoginResponse(String accessToken, String refreshToken, long expiresIn) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.tokenType = "Bearer";
    this.expiresIn = expiresIn;
  }
}
