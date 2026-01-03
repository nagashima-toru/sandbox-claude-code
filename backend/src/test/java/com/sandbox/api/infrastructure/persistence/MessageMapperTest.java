package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class MessageMapperTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MessageMapper messageMapper;

    @Test
    void findByCode_withExistingCode_returnsMessage() {
        // Arrange & Act
        Message result = messageMapper.findByCode("hello");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("hello");
        assertThat(result.getContent()).isEqualTo("Hello, World!");
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void findByCode_withNonExistentCode_returnsNull() {
        // Arrange & Act
        Message result = messageMapper.findByCode("nonexistent");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void findByCode_withNullCode_returnsNull() {
        // Arrange & Act
        Message result = messageMapper.findByCode(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void findByCode_withEmptyCode_returnsNull() {
        // Arrange & Act
        Message result = messageMapper.findByCode("");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void findByCode_isCaseSensitive() {
        // Arrange & Act
        Message result = messageMapper.findByCode("HELLO");

        // Assert
        assertThat(result).isNull();
    }
}