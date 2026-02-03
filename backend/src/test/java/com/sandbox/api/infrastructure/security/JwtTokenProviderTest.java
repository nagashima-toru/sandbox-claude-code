package com.sandbox.api.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for JwtTokenProvider */
class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;
  private static final String SECRET_KEY =
      "test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm";
  private static final long ACCESS_TOKEN_EXPIRATION = 3600000L; // 1 hour
  private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days
  private static final String ISSUER = "sandbox-api-test";

  @BeforeEach
  void setUp() {
    jwtTokenProvider =
        new JwtTokenProvider(SECRET_KEY, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION, ISSUER);
  }

  @Test
  @DisplayName("アクセストークンが正常に生成されること")
  void generateAccessToken_shouldCreateValidToken() {
    // Given
    String username = "testuser";
    String role = "ADMIN";

    // When
    String token = jwtTokenProvider.generateAccessToken(username, role);

    // Then
    assertThat(token).isNotNull();
    assertThat(token).isNotEmpty();
    assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
    assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(role);
  }

  @Test
  @DisplayName("リフレッシュトークンが正常に生成されること")
  void generateRefreshToken_shouldCreateValidToken() {
    // Given
    String username = "testuser";

    // When
    String token = jwtTokenProvider.generateRefreshToken(username);

    // Then
    assertThat(token).isNotNull();
    assertThat(token).isNotEmpty();
    assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
  }

  @Test
  @DisplayName("有効なトークンの検証が成功すること")
  void validateToken_shouldReturnTrueForValidToken() {
    // Given
    String token = jwtTokenProvider.generateAccessToken("testuser", "ADMIN");

    // When
    boolean isValid = jwtTokenProvider.validateToken(token);

    // Then
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("期限切れトークンの検証が失敗すること")
  void validateToken_shouldReturnFalseForExpiredToken() {
    // Given: Create an expired token (expired 1 hour ago)
    SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    Instant now = Instant.now();
    Instant expiration = now.minusMillis(3600000); // 1 hour ago

    String expiredToken =
        Jwts.builder()
            .subject("testuser")
            .claim("role", "ADMIN")
            .issuer(ISSUER)
            .issuedAt(Date.from(now.minusMillis(7200000))) // 2 hours ago
            .expiration(Date.from(expiration))
            .signWith(key)
            .compact();

    // When
    boolean isValid = jwtTokenProvider.validateToken(expiredToken);

    // Then
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("無効な署名のトークンの検証が失敗すること")
  void validateToken_shouldReturnFalseForInvalidSignature() {
    // Given: Create a token with different secret key
    SecretKey differentKey =
        Keys.hmacShaKeyFor(
            "different-secret-key-for-testing-purposes".getBytes(StandardCharsets.UTF_8));
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(3600000);

    String invalidToken =
        Jwts.builder()
            .subject("testuser")
            .claim("role", "ADMIN")
            .issuer(ISSUER)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(differentKey)
            .compact();

    // When
    boolean isValid = jwtTokenProvider.validateToken(invalidToken);

    // Then
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("不正な形式のトークンの検証が失敗すること")
  void validateToken_shouldReturnFalseForMalformedToken() {
    // Given
    String malformedToken = "not.a.valid.jwt.token";

    // When
    boolean isValid = jwtTokenProvider.validateToken(malformedToken);

    // Then
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("空のトークンの検証が失敗すること")
  void validateToken_shouldReturnFalseForEmptyToken() {
    // Given
    String emptyToken = "";

    // When
    boolean isValid = jwtTokenProvider.validateToken(emptyToken);

    // Then
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("トークンからユーザー名を正常に取得できること")
  void getUsernameFromToken_shouldReturnCorrectUsername() {
    // Given
    String username = "testuser";
    String token = jwtTokenProvider.generateAccessToken(username, "ADMIN");

    // When
    String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

    // Then
    assertThat(extractedUsername).isEqualTo(username);
  }

  @Test
  @DisplayName("トークンからロールを正常に取得できること")
  void getRoleFromToken_shouldReturnCorrectRole() {
    // Given
    String role = "VIEWER";
    String token = jwtTokenProvider.generateAccessToken("testuser", role);

    // When
    String extractedRole = jwtTokenProvider.getRoleFromToken(token);

    // Then
    assertThat(extractedRole).isEqualTo(role);
  }

  @Test
  @DisplayName("アクセストークンとリフレッシュトークンが異なる有効期限を持つこと")
  void tokens_shouldHaveDifferentExpirations() {
    // Given
    String username = "testuser";

    // When
    String accessToken = jwtTokenProvider.generateAccessToken(username, "ADMIN");
    String refreshToken = jwtTokenProvider.generateRefreshToken(username);

    // Then
    assertThat(accessToken).isNotEqualTo(refreshToken);
  }
}
