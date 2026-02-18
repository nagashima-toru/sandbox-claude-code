package com.sandbox.api.presentation.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class AuthControllerIntegrationTest {
  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    // Insert test users (each with their own password hash)
    // Use ON CONFLICT UPDATE to ensure password_hash is always fresh for testing
    jdbcTemplate.update(
        "INSERT INTO users (username, password_hash, role, enabled, created_at) "
            + "VALUES (?, ?, 'ADMIN', true, NOW()) "
            + "ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash",
        "testuser",
        passwordEncoder.encode("password123"));
    jdbcTemplate.update(
        "INSERT INTO users (username, password_hash, role, enabled, created_at) "
            + "VALUES (?, ?, 'VIEWER', true, NOW()) "
            + "ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash",
        "viewer",
        passwordEncoder.encode("viewer123"));
  }

  @Test
  void login_withValidCredentials_returns200WithTokens() throws Exception {
    String requestBody =
        """
        {
            "username": "testuser",
            "password": "password123"
        }
        """;

    mockMvc
        .perform(
            post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.accessToken", notNullValue()))
        .andExpect(jsonPath("$.refreshToken", notNullValue()))
        .andExpect(jsonPath("$.tokenType", is("Bearer")))
        .andExpect(jsonPath("$.expiresIn", is(3600)));
  }

  @Test
  void login_withInvalidPassword_returns401() throws Exception {
    String requestBody =
        """
        {
            "username": "testuser",
            "password": "wrongpassword"
        }
        """;

    mockMvc
        .perform(
            post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_withNonexistentUser_returns401() throws Exception {
    String requestBody =
        """
        {
            "username": "nonexistent",
            "password": "password123"
        }
        """;

    mockMvc
        .perform(
            post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_withInvalidData_returns400() throws Exception {
    String requestBody =
        """
        {
            "username": "",
            "password": "short"
        }
        """;

    mockMvc
        .perform(
            post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void refreshToken_withValidToken_returns200WithNewAccessToken() throws Exception {
    // First, login to get a refresh token
    String loginRequest =
        """
        {
            "username": "testuser",
            "password": "password123"
        }
        """;

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
            .andExpect(status().isOk())
            .andReturn();

    String loginResponse = loginResult.getResponse().getContentAsString();
    var loginResponseMap = objectMapper.readValue(loginResponse, java.util.Map.class);
    String refreshToken = (String) loginResponseMap.get("refreshToken");

    // Use the refresh token to get a new access token
    String refreshRequest = String.format("{\"refreshToken\":\"%s\"}", refreshToken);

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken", notNullValue()))
        .andExpect(jsonPath("$.refreshToken", is(refreshToken)))
        .andExpect(jsonPath("$.tokenType", is("Bearer")))
        .andExpect(jsonPath("$.expiresIn", is(3600)));
  }

  @Test
  void refreshToken_withInvalidToken_returns401() throws Exception {
    String requestBody =
        """
        {
            "refreshToken": "invalid-token"
        }
        """;

    mockMvc
        .perform(
            post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logout_withRefreshToken_returns204() throws Exception {
    // First, login to get a refresh token
    String loginRequest =
        """
        {
            "username": "testuser",
            "password": "password123"
        }
        """;

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
            .andExpect(status().isOk())
            .andReturn();

    String loginResponse = loginResult.getResponse().getContentAsString();
    var loginResponseMap = objectMapper.readValue(loginResponse, java.util.Map.class);
    String refreshToken = (String) loginResponseMap.get("refreshToken");

    // Logout with the refresh token
    String logoutRequest = String.format("{\"refreshToken\":\"%s\"}", refreshToken);

    mockMvc
        .perform(
            post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(logoutRequest))
        .andExpect(status().isNoContent());

    // Verify that the refresh token is invalidated by trying to use it again
    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutRequest))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getCurrentUser_withAdminUser_returns200WithAdminRole() throws Exception {
    // Login to get access token
    String loginRequest =
        """
        {
            "username": "testuser",
            "password": "password123"
        }
        """;

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
            .andExpect(status().isOk())
            .andReturn();

    String loginResponse = loginResult.getResponse().getContentAsString();
    var loginResponseMap = objectMapper.readValue(loginResponse, java.util.Map.class);
    String accessToken = (String) loginResponseMap.get("accessToken");

    // Get current user info
    mockMvc
        .perform(get("/api/users/me").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.username", is("testuser")))
        .andExpect(jsonPath("$.role", is("ADMIN")));
  }

  @Test
  void getCurrentUser_withViewerUser_returns200WithViewerRole() throws Exception {
    // Login to get access token
    String loginRequest =
        """
        {
            "username": "viewer",
            "password": "viewer123"
        }
        """;

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
            .andExpect(status().isOk())
            .andReturn();

    String loginResponse = loginResult.getResponse().getContentAsString();
    var loginResponseMap = objectMapper.readValue(loginResponse, java.util.Map.class);
    String accessToken = (String) loginResponseMap.get("accessToken");

    // Get current user info
    mockMvc
        .perform(get("/api/users/me").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.username", is("viewer")))
        .andExpect(jsonPath("$.role", is("VIEWER")));
  }

  @Test
  void getCurrentUser_withoutAuthentication_returns401() throws Exception {
    mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void getCurrentUser_withInvalidToken_returns401() throws Exception {
    mockMvc
        .perform(get("/api/users/me").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());
  }
}
