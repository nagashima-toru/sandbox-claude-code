package com.sandbox.api.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** JWT token provider for generating and validating JWT tokens */
@Component
public class JwtTokenProvider {

  private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

  private final SecretKey secretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;
  private final String issuer;

  public JwtTokenProvider(
      @Value("${security.jwt.secret-key}") String secretKeyString,
      @Value("${security.jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${security.jwt.refresh-token-expiration}") long refreshTokenExpiration,
      @Value("${security.jwt.issuer}") String issuer) {
    this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
    this.issuer = issuer;
  }

  /**
   * Generate an access token for the given username and role
   *
   * @param username username
   * @param role user role
   * @return JWT access token
   */
  public String generateAccessToken(String username, String role) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(accessTokenExpiration);

    return Jwts.builder()
        .subject(username)
        .claim("role", role)
        .issuer(issuer)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .signWith(secretKey)
        .compact();
  }

  /**
   * Generate a refresh token for the given username
   *
   * @param username username
   * @return JWT refresh token
   */
  public String generateRefreshToken(String username) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(refreshTokenExpiration);

    return Jwts.builder()
        .subject(username)
        .issuer(issuer)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .signWith(secretKey)
        .compact();
  }

  /**
   * Validate the given JWT token
   *
   * @param token JWT token
   * @return true if valid, false otherwise
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      return true;
    } catch (SignatureException e) {
      logger.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }

  /**
   * Get username from JWT token
   *
   * @param token JWT token
   * @return username
   */
  public String getUsernameFromToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    return claims.getSubject();
  }

  /**
   * Get role from JWT token
   *
   * @param token JWT token
   * @return role
   */
  public String getRoleFromToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    return claims.get("role", String.class);
  }
}
