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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void getAllMessages_returnsAllMessages() throws Exception {
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].code", is("hello")))
                .andExpect(jsonPath("$[0].content", is("Hello, World!")));
    }

    @Test
    void getMessageById_whenExists_returns200() throws Exception {
        mockMvc.perform(get("/api/messages/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("hello")))
                .andExpect(jsonPath("$.content", is("Hello, World!")));
    }

    @Test
    void getMessageById_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/messages/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void createMessage_withValidData_returns201WithLocation() throws Exception {
        String requestBody = """
                {
                    "code": "new-message",
                    "content": "New Message Content"
                }
                """;

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.code", is("new-message")))
                .andExpect(jsonPath("$.content", is("New Message Content")));
    }

    @Test
    void createMessage_withInvalidData_returns400() throws Exception {
        String requestBody = """
                {
                    "code": "invalid@code!",
                    "content": ""
                }
                """;

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    void createMessage_withDuplicateCode_returns409() throws Exception {
        String requestBody = """
                {
                    "code": "hello",
                    "content": "Duplicate Content"
                }
                """;

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    @Test
    void updateMessage_withValidData_returns200() throws Exception {
        String requestBody = """
                {
                    "code": "updated-hello",
                    "content": "Updated Content"
                }
                """;

        mockMvc.perform(put("/api/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("updated-hello")))
                .andExpect(jsonPath("$.content", is("Updated Content")));
    }

    @Test
    void updateMessage_whenNotFound_returns404() throws Exception {
        String requestBody = """
                {
                    "code": "updated-code",
                    "content": "Updated Content"
                }
                """;

        mockMvc.perform(put("/api/messages/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void updateMessage_withDuplicateCode_returns409() throws Exception {
        // First create a second message
        String createBody = """
                {
                    "code": "test-duplicate",
                    "content": "Test Duplicate"
                }
                """;
        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
                .andExpect(status().isCreated());

        // Then try to update message 1 with the duplicate code
        String updateBody = """
                {
                    "code": "test-duplicate",
                    "content": "Updated Content"
                }
                """;

        mockMvc.perform(put("/api/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void deleteMessage_whenExists_returns204() throws Exception {
        mockMvc.perform(delete("/api/messages/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMessage_whenNotFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/messages/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)));
    }
}
