package com.sandbox.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void createNew_withValidData_createsUser() {
    // Arrange & Act
    User user = User.createNew("admin", "hashedPassword123", Role.ADMIN);

    // Assert
    assertThat(user.getUsername()).isEqualTo("admin");
    assertThat(user.getPasswordHash()).isEqualTo("hashedPassword123");
    assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    assertThat(user.isEnabled()).isTrue();
    assertThat(user.getCreatedAt()).isNotNull();
    assertThat(user.getId()).isNull();
    assertThat(user.getUpdatedAt()).isNull();
  }

  @Test
  void createNew_withNullUsername_throwsException() {
    assertThatThrownBy(() -> User.createNew(null, "hashedPassword", Role.ADMIN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username cannot be null or blank");
  }

  @Test
  void createNew_withBlankUsername_throwsException() {
    assertThatThrownBy(() -> User.createNew("   ", "hashedPassword", Role.ADMIN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username cannot be null or blank");
  }

  @Test
  void createNew_withUsernameTooLong_throwsException() {
    String longUsername = "a".repeat(51);
    assertThatThrownBy(() -> User.createNew(longUsername, "hashedPassword", Role.ADMIN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username must be 50 characters or less");
  }

  @Test
  void createNew_withNullPasswordHash_throwsException() {
    assertThatThrownBy(() -> User.createNew("admin", null, Role.ADMIN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password hash cannot be null or blank");
  }

  @Test
  void createNew_withBlankPasswordHash_throwsException() {
    assertThatThrownBy(() -> User.createNew("admin", "   ", Role.ADMIN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password hash cannot be null or blank");
  }

  @Test
  void createNew_withNullRole_throwsException() {
    assertThatThrownBy(() -> User.createNew("admin", "hashedPassword", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Role cannot be null");
  }

  @Test
  void updatePassword_withValidData_createsNewInstance() {
    // Arrange
    User original = User.createNew("admin", "oldHash", Role.ADMIN);

    // Act
    User updated = original.updatePassword("newHash");

    // Assert - Original instance is unchanged
    assertThat(original.getPasswordHash()).isEqualTo("oldHash");
    assertThat(original.getUpdatedAt()).isNull();

    // Assert - New instance has updated values
    assertThat(updated.getPasswordHash()).isEqualTo("newHash");
    assertThat(updated.getUpdatedAt()).isNotNull();
    assertThat(updated.getUsername()).isEqualTo(original.getUsername());
    assertThat(updated.getRole()).isEqualTo(original.getRole());
    assertThat(updated.isEnabled()).isEqualTo(original.isEnabled());
    assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());

    // Assert - They are different instances
    assertThat(updated).isNotSameAs(original);
  }

  @Test
  void updatePassword_withInvalidPassword_throwsException() {
    User user = User.createNew("admin", "hashedPassword", Role.ADMIN);

    assertThatThrownBy(() -> user.updatePassword(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password hash cannot be null or blank");
  }

  @Test
  void updateRole_withValidData_createsNewInstance() {
    // Arrange
    User original = User.createNew("admin", "hashedPassword", Role.ADMIN);

    // Act
    User updated = original.updateRole(Role.VIEWER);

    // Assert - Original instance is unchanged
    assertThat(original.getRole()).isEqualTo(Role.ADMIN);
    assertThat(original.getUpdatedAt()).isNull();

    // Assert - New instance has updated values
    assertThat(updated.getRole()).isEqualTo(Role.VIEWER);
    assertThat(updated.getUpdatedAt()).isNotNull();
    assertThat(updated.getUsername()).isEqualTo(original.getUsername());
    assertThat(updated.getPasswordHash()).isEqualTo(original.getPasswordHash());
    assertThat(updated.isEnabled()).isEqualTo(original.isEnabled());
    assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());

    // Assert - They are different instances
    assertThat(updated).isNotSameAs(original);
  }

  @Test
  void updateRole_withNullRole_throwsException() {
    User user = User.createNew("admin", "hashedPassword", Role.ADMIN);

    assertThatThrownBy(() -> user.updateRole(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Role cannot be null");
  }

  @Test
  void updateEnabled_withFalse_createsNewInstance() {
    // Arrange
    User original = User.createNew("admin", "hashedPassword", Role.ADMIN);

    // Act
    User updated = original.updateEnabled(false);

    // Assert - Original instance is unchanged
    assertThat(original.isEnabled()).isTrue();
    assertThat(original.getUpdatedAt()).isNull();

    // Assert - New instance has updated values
    assertThat(updated.isEnabled()).isFalse();
    assertThat(updated.getUpdatedAt()).isNotNull();
    assertThat(updated.getUsername()).isEqualTo(original.getUsername());
    assertThat(updated.getPasswordHash()).isEqualTo(original.getPasswordHash());
    assertThat(updated.getRole()).isEqualTo(original.getRole());
    assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());

    // Assert - They are different instances
    assertThat(updated).isNotSameAs(original);
  }

  @Test
  void withId_createsNewInstanceWithId() {
    // Arrange
    User original = User.createNew("admin", "hashedPassword", Role.ADMIN);

    // Act
    User withId = original.withId(123L);

    // Assert - Original instance is unchanged
    assertThat(original.getId()).isNull();

    // Assert - New instance has ID
    assertThat(withId.getId()).isEqualTo(123L);
    assertThat(withId.getUsername()).isEqualTo(original.getUsername());
    assertThat(withId.getPasswordHash()).isEqualTo(original.getPasswordHash());
    assertThat(withId.getRole()).isEqualTo(original.getRole());
    assertThat(withId.isEnabled()).isEqualTo(original.isEnabled());

    // Assert - They are different instances
    assertThat(withId).isNotSameAs(original);
  }

  @Test
  void builder_createsUser() {
    // Arrange & Act
    User user =
        User.builder()
            .id(1L)
            .username("admin")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .enabled(true)
            .build();

    // Assert
    assertThat(user.getId()).isEqualTo(1L);
    assertThat(user.getUsername()).isEqualTo("admin");
    assertThat(user.getPasswordHash()).isEqualTo("hashedPassword");
    assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  void toBuilder_createsNewInstanceWithModifications() {
    // Arrange
    User original = User.createNew("admin", "hashedPassword", Role.ADMIN).withId(1L);

    // Act
    User modified = original.toBuilder().role(Role.VIEWER).build();

    // Assert - Original is unchanged
    assertThat(original.getRole()).isEqualTo(Role.ADMIN);

    // Assert - New instance has modifications
    assertThat(modified.getRole()).isEqualTo(Role.VIEWER);
    assertThat(modified.getId()).isEqualTo(original.getId());
    assertThat(modified.getUsername()).isEqualTo(original.getUsername());
    assertThat(modified.getPasswordHash()).isEqualTo(original.getPasswordHash());

    // Assert - They are different instances
    assertThat(modified).isNotSameAs(original);
  }

  @Test
  void equals_basedOnId() {
    User user1 = User.createNew("admin1", "hash1", Role.ADMIN).withId(1L);
    User user2 = User.createNew("admin2", "hash2", Role.VIEWER).withId(1L);
    User user3 = User.createNew("admin1", "hash1", Role.ADMIN).withId(2L);

    // Same ID should be equal
    assertThat(user1).isEqualTo(user2);

    // Different ID should not be equal
    assertThat(user1).isNotEqualTo(user3);
  }
}
