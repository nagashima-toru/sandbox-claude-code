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

/**
 * Integration tests for Message API authorization
 *
 * <p>Task 4.3: Test role-based access control - ADMIN can perform all operations - VIEWER can only
 * read - Unauthenticated requests receive 401
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class MessageAuthorizationTest {
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

  private String adminToken;
  private String viewerToken;

  @BeforeEach
  void setUp() throws Exception {
    // Insert test users: ADMIN and VIEWER (use unique usernames to avoid conflicts)
    String adminPassword = passwordEncoder.encode("testadmin123");
    String viewerPassword = passwordEncoder.encode("testviewer123");

    jdbcTemplate.update(
        "INSERT INTO users (username, password_hash, role, enabled, created_at) "
            + "VALUES (?, ?, 'ADMIN', true, NOW()) ON CONFLICT DO NOTHING",
        "testadmin",
        adminPassword);

    jdbcTemplate.update(
        "INSERT INTO users (username, password_hash, role, enabled, created_at) "
            + "VALUES (?, ?, 'VIEWER', true, NOW()) ON CONFLICT DO NOTHING",
        "testviewer",
        viewerPassword);

    // Insert test message (delete first to ensure clean state)
    jdbcTemplate.update("DELETE FROM messages WHERE id = 999");
    jdbcTemplate.update(
        "INSERT INTO messages (id, code, content) VALUES (999, 'TEST001', 'Test message')");

    // Get tokens
    adminToken = loginAndGetToken("testadmin", "testadmin123");
    viewerToken = loginAndGetToken("testviewer", "testviewer123");
  }

  private String loginAndGetToken(String username, String password) throws Exception {
    String requestBody =
        String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    return objectMapper.readTree(responseBody).get("accessToken").asText();
  }

  // ========== Unauthenticated Access Tests ==========

  @Test
  void getAllMessages_withoutToken_returns401() throws Exception {
    mockMvc
        .perform(get("/api/messages"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status", is(401)))
        .andExpect(jsonPath("$.detail", notNullValue()))
        .andExpect(jsonPath("$.instance", notNullValue()));
  }

  @Test
  void createMessage_withoutToken_returns401() throws Exception {
    String requestBody = "{\"code\": \"NEW001\", \"content\": \"New message\"}";

    mockMvc
        .perform(post("/api/messages").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  // ========== ADMIN Role Tests ==========

  @Test
  void getAllMessages_withAdminToken_returns200() throws Exception {
    mockMvc
        .perform(get("/api/messages").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getMessageById_withAdminToken_returns200() throws Exception {
    mockMvc
        .perform(get("/api/messages/999").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code", is("TEST001")));
  }

  @Test
  void createMessage_withAdminToken_returns201() throws Exception {
    String requestBody = "{\"code\": \"ADMIN001\", \"content\": \"Admin created message\"}";

    mockMvc
        .perform(
            post("/api/messages")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code", is("ADMIN001")))
        .andExpect(header().exists("Location"));
  }

  @Test
  void updateMessage_withAdminToken_returns200() throws Exception {
    String requestBody = "{\"code\": \"UPDATED001\", \"content\": \"Updated message\"}";

    mockMvc
        .perform(
            put("/api/messages/999")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code", is("UPDATED001")));
  }

  @Test
  void deleteMessage_withAdminToken_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/messages/999").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isNoContent());
  }

  // ========== VIEWER Role Tests ==========

  @Test
  void getAllMessages_withViewerToken_returns200() throws Exception {
    mockMvc
        .perform(get("/api/messages").header("Authorization", "Bearer " + viewerToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getMessageById_withViewerToken_returns200() throws Exception {
    mockMvc
        .perform(get("/api/messages/999").header("Authorization", "Bearer " + viewerToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code", is("TEST001")));
  }

  @Test
  void createMessage_withViewerToken_returns403() throws Exception {
    String requestBody = "{\"code\": \"VIEWER001\", \"content\": \"Viewer created message\"}";

    mockMvc
        .perform(
            post("/api/messages")
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status", is(403)))
        .andExpect(jsonPath("$.detail", notNullValue()))
        .andExpect(jsonPath("$.instance", notNullValue()));
  }

  @Test
  void updateMessage_withViewerToken_returns403() throws Exception {
    String requestBody = "{\"code\": \"VIEWER002\", \"content\": \"Viewer updated message\"}";

    mockMvc
        .perform(
            put("/api/messages/999")
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status", is(403)))
        .andExpect(jsonPath("$.detail", notNullValue()));
  }

  @Test
  void deleteMessage_withViewerToken_returns403() throws Exception {
    mockMvc
        .perform(delete("/api/messages/999").header("Authorization", "Bearer " + viewerToken))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status", is(403)))
        .andExpect(jsonPath("$.detail", notNullValue()));
  }
}
