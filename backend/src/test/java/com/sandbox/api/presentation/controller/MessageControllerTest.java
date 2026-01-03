package com.sandbox.api.presentation.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class MessageControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void getMessage_whenMessageExists_returns200WithContent() throws Exception {
        // Arrange: test-data.sql contains 'hello' message

        // Act & Assert
        mockMvc.perform(get("/api/message"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void getMessage_whenMessageNotFound_returnsNotFoundMessage() throws Exception {
        // Arrange: Delete the 'hello' message to simulate not found scenario
        jdbcTemplate.update("DELETE FROM messages WHERE code = ?", "hello");

        // Act & Assert
        mockMvc.perform(get("/api/message"))
                .andExpect(status().isOk())
                .andExpect(content().string("Message not found"));
    }

    @Test
    void getMessage_withGetRequest_succeeds() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(get("/api/message")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk());
    }

    @Test
    void getMessage_checkEndpointPath() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(get("/api/message"))
                .andExpect(status().isOk());
    }

    @Test
    void getMessage_invalidPath_returns404() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isNotFound());
    }
}
