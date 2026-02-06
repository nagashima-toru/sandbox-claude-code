package com.sandbox.api.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Unit tests for JwtAuthenticationFilter */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() {
    jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("有効なJWTトークンで認証が成功すること")
  void doFilterInternal_shouldAuthenticateWithValidToken() throws Exception {
    // Given
    String token = "valid.jwt.token";
    String username = "testuser";
    String role = "ADMIN";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);
    when(jwtTokenProvider.getRoleFromToken(token)).thenReturn(role);

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal()).isEqualTo(username);
    assertThat(authentication.getAuthorities()).hasSize(1);
    assertThat(authentication.getAuthorities().iterator().next().getAuthority())
        .isEqualTo("ROLE_ADMIN");

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Authorizationヘッダーがない場合は認証をスキップすること")
  void doFilterInternal_shouldSkipAuthenticationWhenNoAuthorizationHeader() throws Exception {
    // Given
    when(request.getHeader("Authorization")).thenReturn(null);

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNull();

    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Bearer prefixがない場合は認証をスキップすること")
  void doFilterInternal_shouldSkipAuthenticationWhenNoBearerPrefix() throws Exception {
    // Given
    when(request.getHeader("Authorization")).thenReturn("InvalidPrefix token");

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNull();

    verify(jwtTokenProvider, never()).validateToken(anyString());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("無効なトークンの場合は認証をスキップすること")
  void doFilterInternal_shouldSkipAuthenticationWhenTokenIsInvalid() throws Exception {
    // Given
    String token = "invalid.jwt.token";
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(false);

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNull();

    verify(jwtTokenProvider).validateToken(token);
    verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("トークン処理中に例外が発生しても次のフィルターが実行されること")
  void doFilterInternal_shouldContinueFilterChainWhenExceptionOccurs() throws Exception {
    // Given
    String token = "valid.jwt.token";
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token))
        .thenThrow(new RuntimeException("Token validation error"));

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNull();

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("ViewerロールでAuthenticationが設定されること")
  void doFilterInternal_shouldAuthenticateWithViewerRole() throws Exception {
    // Given
    String token = "valid.jwt.token";
    String username = "viewer";
    String role = "VIEWER";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);
    when(jwtTokenProvider.getRoleFromToken(token)).thenReturn(role);

    // When
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal()).isEqualTo(username);
    assertThat(authentication.getAuthorities()).hasSize(1);
    assertThat(authentication.getAuthorities().iterator().next().getAuthority())
        .isEqualTo("ROLE_VIEWER");

    verify(filterChain).doFilter(request, response);
  }
}
