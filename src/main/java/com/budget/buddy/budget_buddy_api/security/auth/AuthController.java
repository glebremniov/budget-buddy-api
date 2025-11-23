package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.api.AuthApi;
import com.budget.buddy.budget_buddy_api.model.AuthToken;
import com.budget.buddy.budget_buddy_api.model.V1AuthLoginPostRequest;
import com.budget.buddy.budget_buddy_api.model.V1AuthRefreshPostRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller implementing the generated AuthApi interface. Handles user login and token refresh operations.
 */
@RestController
public class AuthController implements AuthApi {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * Authenticate user and receive access + refresh tokens
   *
   * @param v1AuthLoginPostRequest login credentials (username, password)
   * @return AuthToken with access_token, refresh_token, and expires_in
   */
  @Override
  public ResponseEntity<AuthToken> v1AuthLoginPost(@Valid V1AuthLoginPostRequest v1AuthLoginPostRequest) {

    var token = authService.login(v1AuthLoginPostRequest.getUsername(), v1AuthLoginPostRequest.getPassword());

    return ResponseEntity.ok(token);
  }

  /**
   * Exchange a refresh token for a new access token
   *
   * @param v1AuthRefreshPostRequest request containing refresh_token
   * @return AuthToken with new access_token
   */
  @Override
  public ResponseEntity<AuthToken> v1AuthRefreshPost(@Valid V1AuthRefreshPostRequest v1AuthRefreshPostRequest) {

    var token = authService.refresh(v1AuthRefreshPostRequest.getRefreshToken());

    return ResponseEntity.ok(token);
  }
}
