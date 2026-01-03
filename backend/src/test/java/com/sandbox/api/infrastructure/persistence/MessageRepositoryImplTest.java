package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageRepositoryImplTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageRepositoryImpl repository;

    @Test
    void findByCode_whenMapperReturnsMessage_returnsOptionalWithMessage() {
        // Arrange
        Message expected = new Message(1L, "test-code", "Test Message");
        when(messageMapper.findByCode("test-code")).thenReturn(expected);

        // Act
        Optional<Message> result = repository.findByCode("test-code");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
        verify(messageMapper).findByCode("test-code");
    }

    @Test
    void findByCode_whenMapperReturnsNull_returnsEmptyOptional() {
        // Arrange
        when(messageMapper.findByCode("nonexistent")).thenReturn(null);

        // Act
        Optional<Message> result = repository.findByCode("nonexistent");

        // Assert
        assertThat(result).isEmpty();
        verify(messageMapper).findByCode("nonexistent");
    }

    @Test
    void findByCode_withNullCode_returnsEmptyOptionalWhenMapperReturnsNull() {
        // Arrange
        when(messageMapper.findByCode(null)).thenReturn(null);

        // Act
        Optional<Message> result = repository.findByCode(null);

        // Assert
        assertThat(result).isEmpty();
        verify(messageMapper).findByCode(null);
    }

    @Test
    void findByCode_callsMapperExactlyOnce() {
        // Arrange
        String code = "hello";
        Message message = new Message(1L, code, "Hello, World!");
        when(messageMapper.findByCode(code)).thenReturn(message);

        // Act
        repository.findByCode(code);

        // Assert
        verify(messageMapper).findByCode(code);
    }
}
