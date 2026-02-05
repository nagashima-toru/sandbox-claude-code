package com.sandbox.api.presentation.controller;

import com.sandbox.api.application.usecase.auth.LoginUseCase;
import com.sandbox.api.application.usecase.auth.LogoutUseCase;
import com.sandbox.api.application.usecase.auth.RefreshTokenUseCase;
import com.sandbox.api.presentation.dto.AuthMapper;
import com.sandbox.api.presentation.generated.api.AuthApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** REST controller implementing the OpenAPI generated AuthApi interface. */
@RestController
public class AuthController implements AuthApi {

  private final LoginUseCase loginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;

  /**
   * Constructs a new AuthController with the required use cases.
   *
   * @param loginUseCase use case for user login
   * @param refreshTokenUseCase use case for refreshing access token
   * @param logoutUseCase use case for user logout
   */
  public AuthController(
      LoginUseCase loginUseCase,
      RefreshTokenUseCase refreshTokenUseCase,
      LogoutUseCase logoutUseCase) {
    this.loginUseCase = loginUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
    this.logoutUseCase = logoutUseCase;
  }

  @Override
  public ResponseEntity<com.sandbox.api.presentation.generated.model.LoginResponse> login(
      com.sandbox.api.presentation.generated.model.LoginRequest loginRequest) {
    com.sandbox.api.application.dto.LoginRequest internal = AuthMapper.toInternal(loginRequest);
    com.sandbox.api.application.dto.LoginResponse response =
        loginUseCase.execute(internal.getUsername(), internal.getPassword());
    return ResponseEntity.ok(AuthMapper.toGenerated(response));
  }

  @Override
  public ResponseEntity<com.sandbox.api.presentation.generated.model.LoginResponse> refreshToken(
      com.sandbox.api.presentation.generated.model.RefreshRequest refreshRequest) {
    com.sandbox.api.application.dto.RefreshRequest internal = AuthMapper.toInternal(refreshRequest);
    com.sandbox.api.application.dto.LoginResponse response =
        refreshTokenUseCase.execute(internal.getRefreshToken());
    return ResponseEntity.ok(AuthMapper.toGenerated(response));
  }

  @Override
  public ResponseEntity<Void> logout(
      com.sandbox.api.presentation.generated.model.RefreshRequest refreshRequest) {
    com.sandbox.api.application.dto.RefreshRequest internal = AuthMapper.toInternal(refreshRequest);
    logoutUseCase.execute(internal.getRefreshToken());
    return ResponseEntity.noContent().build();
  }
}
