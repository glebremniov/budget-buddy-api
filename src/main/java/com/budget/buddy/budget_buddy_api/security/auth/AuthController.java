package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.generated.api.AuthApi;
import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.generated.model.V1AuthLoginPostRequest;
import com.budget.buddy.budget_buddy_api.generated.model.V1AuthRefreshPostRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller implementing the generated AuthApi interface. Handles user login and token refresh operations.
 */
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final AuthService authService;

  /**
   * Authenticate user and receive access + refresh tokens
   *
   * @param request login credentials (username, password)
   * @return AuthToken with access_token, refresh_token, and expires_in
   */
  @Override
  public ResponseEntity<AuthToken> v1AuthLoginPost(@Valid V1AuthLoginPostRequest request) {
    var token = authService.login(request.getUsername(), request.getPassword());
    return ResponseEntity.ok(token);
  }

  /**
   * Exchange a refresh token for a new access token
   *
   * @param request request containing refresh_token
   * @return AuthToken with new access_token
   */
  @Override
  public ResponseEntity<AuthToken> v1AuthRefreshPost(@Valid V1AuthRefreshPostRequest request) {
    var token = authService.refresh(request.getRefreshToken());
    return ResponseEntity.ok(token);
  }
}
