package com.sandbox.api.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.sandbox.api.domain.model.Role;
import com.sandbox.api.domain.model.User;
import com.sandbox.api.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Unit tests for CustomUserDetailsService */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock private UserRepository userRepository;

  private CustomUserDetailsService customUserDetailsService;

  @BeforeEach
  void setUp() {
    customUserDetailsService = new CustomUserDetailsService(userRepository);
  }

  @Test
  @DisplayName("ユーザー名でユーザーが正常にロードされること")
  void loadUserByUsername_shouldReturnUserDetails() {
    // Given
    String username = "admin";
    String passwordHash = "$2a$10$abcdefghijklmnopqrstuvwxyz";
    User user =
        User.builder()
            .id(1L)
            .username(username)
            .passwordHash(passwordHash)
            .role(Role.ADMIN)
            .enabled(true)
            .build();

    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    // When
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

    // Then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(username);
    assertThat(userDetails.getPassword()).isEqualTo(passwordHash);
    assertThat(userDetails.getAuthorities()).hasSize(1);
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
        .isEqualTo("ROLE_ADMIN");
    assertThat(userDetails.isEnabled()).isTrue();
    assertThat(userDetails.isAccountNonExpired()).isTrue();
    assertThat(userDetails.isAccountNonLocked()).isTrue();
    assertThat(userDetails.isCredentialsNonExpired()).isTrue();
  }

  @Test
  @DisplayName("VIEWERロールのユーザーが正常にロードされること")
  void loadUserByUsername_shouldReturnUserDetailsForViewer() {
    // Given
    String username = "viewer";
    String passwordHash = "$2a$10$abcdefghijklmnopqrstuvwxyz";
    User user =
        User.builder()
            .id(2L)
            .username(username)
            .passwordHash(passwordHash)
            .role(Role.VIEWER)
            .enabled(true)
            .build();

    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    // When
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

    // Then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(username);
    assertThat(userDetails.getAuthorities()).hasSize(1);
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
        .isEqualTo("ROLE_VIEWER");
  }

  @Test
  @DisplayName("無効化されたユーザーがロードされること")
  void loadUserByUsername_shouldReturnDisabledUserDetails() {
    // Given
    String username = "disabled";
    String passwordHash = "$2a$10$abcdefghijklmnopqrstuvwxyz";
    User user =
        User.builder()
            .id(3L)
            .username(username)
            .passwordHash(passwordHash)
            .role(Role.ADMIN)
            .enabled(false)
            .build();

    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    // When
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

    // Then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.isEnabled()).isFalse();
  }

  @Test
  @DisplayName("存在しないユーザー名でUsernameNotFoundExceptionがスローされること")
  void loadUserByUsername_shouldThrowExceptionWhenUserNotFound() {
    // Given
    String username = "nonexistent";
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found with username: " + username);
  }
}
