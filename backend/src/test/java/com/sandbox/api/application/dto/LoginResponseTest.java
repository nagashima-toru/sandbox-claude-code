package com.sandbox.api.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoginResponseTest {

  @Test
  void noArgsConstructor_createsInstanceWithDefaults() {
    LoginResponse response = new LoginResponse();

    assertThat(response.getTokenType()).isEqualTo("Bearer");
  }

  @Test
  void allArgsConstructor_createsInstanceWithProvidedValues() {
    LoginResponse response =
        new LoginResponse("access-token-123", "refresh-token-456", "Custom", 7200);

    assertThat(response.getAccessToken()).isEqualTo("access-token-123");
    assertThat(response.getRefreshToken()).isEqualTo("refresh-token-456");
    assertThat(response.getTokenType()).isEqualTo("Custom");
    assertThat(response.getExpiresIn()).isEqualTo(7200);
  }

  @Test
  void threeArgsConstructor_createsInstanceWithBearerTokenType() {
    LoginResponse response = new LoginResponse("access-token-123", "refresh-token-456", 3600);

    assertThat(response.getAccessToken()).isEqualTo("access-token-123");
    assertThat(response.getRefreshToken()).isEqualTo("refresh-token-456");
    assertThat(response.getTokenType()).isEqualTo("Bearer");
    assertThat(response.getExpiresIn()).isEqualTo(3600);
  }

  @Test
  void setters_updateValues() {
    LoginResponse response = new LoginResponse();
    response.setAccessToken("new-access-token");
    response.setRefreshToken("new-refresh-token");
    response.setTokenType("NewType");
    response.setExpiresIn(1800);

    assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    assertThat(response.getTokenType()).isEqualTo("NewType");
    assertThat(response.getExpiresIn()).isEqualTo(1800);
  }
}
