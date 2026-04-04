package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_contracts.generated.api.AuthApi;
import com.budget.buddy.budget_buddy_contracts.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_contracts.generated.model.LoginRequest;
import com.budget.buddy.budget_buddy_contracts.generated.model.RefreshTokenRequest;
import com.budget.buddy.budget_buddy_contracts.generated.model.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller implementing the generated AuthApi interface. Handles user login and token refresh operations.
 */
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final AuthService authService;

  @Override
  public ResponseEntity<AuthToken> loginUser(@Valid LoginRequest request) {
    var token = authService.login(request.getUsername(), request.getPassword());
    return ResponseEntity.ok(token);
  }

  @Override
  public ResponseEntity<AuthToken> refreshToken(@Valid RefreshTokenRequest request) {
    var token = authService.refresh(request.getRefreshToken());
    return ResponseEntity.ok(token);
  }

  @Override
  public ResponseEntity<Void> registerUser(RegisterRequest registerRequest) {
    authService.register(registerRequest);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Override
  public ResponseEntity<Void> logoutUser() {
    authService.logout();
    return ResponseEntity.noContent().build();
  }
}
