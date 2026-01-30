package com.sandbox.api.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.sandbox.api.domain.model.Message;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class MessageRepositoryImplTest {
  @Mock private MessageMapper messageMapper;
  @InjectMocks private MessageRepositoryImpl repository;
  @Test
  void findByCode_whenMapperReturnsMessage_returnsOptionalWithMessage() {
    // Arrange
    Message expected = new Message(1L, "test-code", "Test Message", LocalDateTime.now(), LocalDateTime.now());
    when(messageMapper.findByCode("test-code")).thenReturn(expected);
    // Act
    Optional<Message> result = repository.findByCode("test-code");
    // Assert
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expected);
    verify(messageMapper).findByCode("test-code");
  }
  void findByCode_whenMapperReturnsNull_returnsEmptyOptional() {
    when(messageMapper.findByCode("nonexistent")).thenReturn(null);
    Optional<Message> result = repository.findByCode("nonexistent");
    assertThat(result).isEmpty();
    verify(messageMapper).findByCode("nonexistent");
  void findByCode_withNullCode_returnsEmptyOptionalWhenMapperReturnsNull() {
    when(messageMapper.findByCode(null)).thenReturn(null);
    Optional<Message> result = repository.findByCode(null);
    verify(messageMapper).findByCode(null);
  void findByCode_callsMapperExactlyOnce() {
    String code = "hello";
    Message message = new Message(1L, code, "Hello, World!");
    when(messageMapper.findByCode(code)).thenReturn(message);
    repository.findByCode(code);
    verify(messageMapper).findByCode(code);
  void findAll_whenMapperReturnsMessages_returnsListOfMessages() {
    List<Message> expected =
        Arrays.asList(new Message(1L, "code1", "Content 1", LocalDateTime.now(), LocalDateTime.now()), new Message(2L, "code2", "Content 2", LocalDateTime.now(), LocalDateTime.now()));
    when(messageMapper.findAll()).thenReturn(expected);
    List<Message> result = repository.findAll();
    assertThat(result).hasSize(2);
    assertThat(result).isEqualTo(expected);
    verify(messageMapper).findAll();
  void findById_whenMapperReturnsMessage_returnsOptionalWithMessage() {
    Message expected = new Message(1L, "code", "Content", LocalDateTime.now(), LocalDateTime.now());
    when(messageMapper.findById(1L)).thenReturn(expected);
    Optional<Message> result = repository.findById(1L);
    verify(messageMapper).findById(1L);
  void findById_whenMapperReturnsNull_returnsEmptyOptional() {
    when(messageMapper.findById(99L)).thenReturn(null);
    Optional<Message> result = repository.findById(99L);
    verify(messageMapper).findById(99L);
  void save_whenMessageHasNoId_callsInsert() {
    Message message = new Message(null, "new-code", "New Content", LocalDateTime.now(), LocalDateTime.now());
    Message result = repository.save(message);
    assertThat(result).isEqualTo(message);
    verify(messageMapper).insert(message);
  void save_whenMessageHasId_callsUpdate() {
    Message message = new Message(1L, "code", "Updated Content", LocalDateTime.now(), LocalDateTime.now());
    verify(messageMapper).update(message);
  void deleteById_callsMapperDeleteById() {
    // Arrange & Act
    repository.deleteById(1L);
    verify(messageMapper).deleteById(1L);
  void existsByCode_whenMapperReturnsTrue_returnsTrue() {
    when(messageMapper.existsByCode("existing")).thenReturn(true);
    boolean result = repository.existsByCode("existing");
    assertThat(result).isTrue();
    verify(messageMapper).existsByCode("existing");
  void existsByCode_whenMapperReturnsFalse_returnsFalse() {
    when(messageMapper.existsByCode("nonexistent")).thenReturn(false);
    boolean result = repository.existsByCode("nonexistent");
    assertThat(result).isFalse();
    verify(messageMapper).existsByCode("nonexistent");
}
