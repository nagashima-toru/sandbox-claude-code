package com.sandbox.api.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sandbox.api.domain.model.Role;
import com.sandbox.api.domain.model.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {
  @Mock private UserMapper userMapper;
  @InjectMocks private UserRepositoryImpl repository;

  @Test
  void findByUsername_whenMapperReturnsUser_returnsOptionalWithUser() {
    // Arrange
    User expected =
        User.builder()
            .id(1L)
            .username("admin")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    when(userMapper.findByUsername("admin")).thenReturn(expected);

    // Act
    Optional<User> result = repository.findByUsername("admin");

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expected);
    verify(userMapper).findByUsername("admin");
  }

  @Test
  void findByUsername_whenMapperReturnsNull_returnsEmptyOptional() {
    // Arrange
    when(userMapper.findByUsername("nonexistent")).thenReturn(null);

    // Act
    Optional<User> result = repository.findByUsername("nonexistent");

    // Assert
    assertThat(result).isEmpty();
    verify(userMapper).findByUsername("nonexistent");
  }

  @Test
  void findById_whenMapperReturnsUser_returnsOptionalWithUser() {
    // Arrange
    User expected =
        User.builder()
            .id(1L)
            .username("admin")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    when(userMapper.findById(1L)).thenReturn(expected);

    // Act
    Optional<User> result = repository.findById(1L);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expected);
    verify(userMapper).findById(1L);
  }

  @Test
  void findById_whenMapperReturnsNull_returnsEmptyOptional() {
    // Arrange
    when(userMapper.findById(999L)).thenReturn(null);

    // Act
    Optional<User> result = repository.findById(999L);

    // Assert
    assertThat(result).isEmpty();
    verify(userMapper).findById(999L);
  }

  @Test
  void existsByUsername_whenUserExists_returnsTrue() {
    // Arrange
    when(userMapper.existsByUsername("admin")).thenReturn(true);

    // Act
    boolean result = repository.existsByUsername("admin");

    // Assert
    assertThat(result).isTrue();
    verify(userMapper).existsByUsername("admin");
  }

  @Test
  void existsByUsername_whenUserDoesNotExist_returnsFalse() {
    // Arrange
    when(userMapper.existsByUsername("nonexistent")).thenReturn(false);

    // Act
    boolean result = repository.existsByUsername("nonexistent");

    // Assert
    assertThat(result).isFalse();
    verify(userMapper).existsByUsername("nonexistent");
  }
}
