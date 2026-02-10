package com.sandbox.api.application.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sandbox.api.application.dto.UserResponse;
import com.sandbox.api.domain.model.Role;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

class GetCurrentUserUseCaseTest {

  private final GetCurrentUserUseCase getCurrentUserUseCase = new GetCurrentUserUseCase();

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void execute_withAdminUser_returnsUserResponseWithAdminRole() {
    // Arrange
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            "admin",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    SecurityContext securityContext = new SecurityContextImpl(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Act
    UserResponse response = getCurrentUserUseCase.execute();

    // Assert
    assertThat(response.getUsername()).isEqualTo("admin");
    assertThat(response.getRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  void execute_withViewerUser_returnsUserResponseWithViewerRole() {
    // Arrange
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            "viewer",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER")));
    SecurityContext securityContext = new SecurityContextImpl(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Act
    UserResponse response = getCurrentUserUseCase.execute();

    // Assert
    assertThat(response.getUsername()).isEqualTo("viewer");
    assertThat(response.getRole()).isEqualTo(Role.VIEWER);
  }

  @Test
  void execute_withNullAuthentication_throwsAuthenticationCredentialsNotFoundException() {
    // Arrange
    SecurityContextHolder.clearContext();

    // Act & Assert
    assertThatThrownBy(() -> getCurrentUserUseCase.execute())
        .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
        .hasMessage("User is not authenticated");
  }

  @Test
  void execute_withAnonymousUser_throwsAuthenticationCredentialsNotFoundException() {
    // Arrange
    AnonymousAuthenticationToken authentication =
        new AnonymousAuthenticationToken(
            "anonymous",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContext securityContext = new SecurityContextImpl(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Act & Assert
    assertThatThrownBy(() -> getCurrentUserUseCase.execute())
        .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
        .hasMessage("User is not authenticated");
  }

  @Test
  void execute_withNoRole_throwsIllegalStateException() {
    // Arrange
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("user", "password", Collections.emptyList());
    SecurityContext securityContext = new SecurityContextImpl(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Act & Assert
    assertThatThrownBy(() -> getCurrentUserUseCase.execute())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("User has no role assigned");
  }

  @Test
  void execute_withInvalidRole_throwsIllegalStateException() {
    // Arrange
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            "user",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_INVALID")));
    SecurityContext securityContext = new SecurityContextImpl(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Act & Assert
    assertThatThrownBy(() -> getCurrentUserUseCase.execute())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Invalid role: INVALID");
  }
}
