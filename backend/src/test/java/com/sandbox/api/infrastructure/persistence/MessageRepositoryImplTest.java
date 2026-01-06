package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
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

    @Test
    void findAll_whenMapperReturnsMessages_returnsListOfMessages() {
        // Arrange
        List<Message> expected = Arrays.asList(
                new Message(1L, "code1", "Content 1"),
                new Message(2L, "code2", "Content 2")
        );
        when(messageMapper.findAll()).thenReturn(expected);

        // Act
        List<Message> result = repository.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expected);
        verify(messageMapper).findAll();
    }

    @Test
    void findById_whenMapperReturnsMessage_returnsOptionalWithMessage() {
        // Arrange
        Message expected = new Message(1L, "code", "Content");
        when(messageMapper.findById(1L)).thenReturn(expected);

        // Act
        Optional<Message> result = repository.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
        verify(messageMapper).findById(1L);
    }

    @Test
    void findById_whenMapperReturnsNull_returnsEmptyOptional() {
        // Arrange
        when(messageMapper.findById(99L)).thenReturn(null);

        // Act
        Optional<Message> result = repository.findById(99L);

        // Assert
        assertThat(result).isEmpty();
        verify(messageMapper).findById(99L);
    }

    @Test
    void save_whenMessageHasNoId_callsInsert() {
        // Arrange
        Message message = new Message(null, "new-code", "New Content");

        // Act
        Message result = repository.save(message);

        // Assert
        assertThat(result).isEqualTo(message);
        verify(messageMapper).insert(message);
    }

    @Test
    void save_whenMessageHasId_callsUpdate() {
        // Arrange
        Message message = new Message(1L, "code", "Updated Content");

        // Act
        Message result = repository.save(message);

        // Assert
        assertThat(result).isEqualTo(message);
        verify(messageMapper).update(message);
    }

    @Test
    void deleteById_callsMapperDeleteById() {
        // Arrange & Act
        repository.deleteById(1L);

        // Assert
        verify(messageMapper).deleteById(1L);
    }

    @Test
    void existsByCode_whenMapperReturnsTrue_returnsTrue() {
        // Arrange
        when(messageMapper.existsByCode("existing")).thenReturn(true);

        // Act
        boolean result = repository.existsByCode("existing");

        // Assert
        assertThat(result).isTrue();
        verify(messageMapper).existsByCode("existing");
    }

    @Test
    void existsByCode_whenMapperReturnsFalse_returnsFalse() {
        // Arrange
        when(messageMapper.existsByCode("nonexistent")).thenReturn(false);

        // Act
        boolean result = repository.existsByCode("nonexistent");

        // Assert
        assertThat(result).isFalse();
        verify(messageMapper).existsByCode("nonexistent");
    }
}
