package com.sandbox.api.presentation.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class MessageControllerTest {
  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }
  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Test
  void getAllMessages_returnsAllMessages() throws Exception {
    mockMvc
        .perform(get("/api/messages"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].code", is("hello")))
        .andExpect(jsonPath("$[0].content", is("Hello, World!")));
  void getMessageById_whenExists_returns200() throws Exception {
        .perform(get("/api/messages/1"))
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.code", is("hello")))
        .andExpect(jsonPath("$.content", is("Hello, World!")));
  void getMessageById_whenNotFound_returns404() throws Exception {
        .perform(get("/api/messages/99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status", is(404)))
        .andExpect(jsonPath("$.error", is("Not Found")));
  void createMessage_withValidData_returns201WithLocation() throws Exception {
    String requestBody =
        """
        {
            "code": "new-message",
            "content": "New Message Content"
        }
        """;
        .perform(post("/api/messages").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.code", is("new-message")))
        .andExpect(jsonPath("$.content", is("New Message Content")));
  void createMessage_withInvalidData_returns400() throws Exception {
            "code": "invalid@code!",
            "content": ""
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.error", is("Bad Request")));
  void createMessage_withDuplicateCode_returns409() throws Exception {
            "code": "hello",
            "content": "Duplicate Content"
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status", is(409)))
        .andExpect(jsonPath("$.error", is("Conflict")));
  void updateMessage_withValidData_returns200() throws Exception {
            "code": "updated-hello",
            "content": "Updated Content"
        .perform(
            put("/api/messages/1").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(jsonPath("$.code", is("updated-hello")))
        .andExpect(jsonPath("$.content", is("Updated Content")));
  void updateMessage_whenNotFound_returns404() throws Exception {
            "code": "updated-code",
            put("/api/messages/99999").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(jsonPath("$.status", is(404)));
  void updateMessage_withDuplicateCode_returns409() throws Exception {
    // First create a second message
    String createBody =
            "code": "test-duplicate",
            "content": "Test Duplicate"
        .perform(post("/api/messages").contentType(MediaType.APPLICATION_JSON).content(createBody))
        .andExpect(status().isCreated());
    // Then try to update message 1 with the duplicate code
    String updateBody =
        .perform(put("/api/messages/1").contentType(MediaType.APPLICATION_JSON).content(updateBody))
        .andExpect(jsonPath("$.status", is(409)));
  void deleteMessage_whenExists_returns204() throws Exception {
    mockMvc.perform(delete("/api/messages/1")).andExpect(status().isNoContent());
  void deleteMessage_whenNotFound_returns404() throws Exception {
        .perform(delete("/api/messages/99999"))
}
