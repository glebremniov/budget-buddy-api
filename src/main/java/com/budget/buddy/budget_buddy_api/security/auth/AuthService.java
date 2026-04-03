package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import com.budget.buddy.budget_buddy_api.security.auth.token.AuthTokenService;
import com.budget.buddy.budget_buddy_api.security.refresh.token.RefreshTokenService;
import com.budget.buddy.budget_buddy_api.user.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations. Handles user registration, login, token refresh and logout.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserService userService;
  private final RefreshTokenService refreshTokenService;
  private final AuthTokenService authTokenService;

  /**
   * Register a new user with default role
   *
   * @param request registration request containing username and password
   * @throws DataIntegrityViolationException if username is already taken
   */
  @Transactional
  public void register(RegisterRequest request) {
    if (userService.existsByUsername(request.getUsername())) {
      throw new DataIntegrityViolationException("Username already taken");
    }

    userService.create(request);
  }

  /**
   * Authenticate user and issue access + opaque refresh token
   *
   * @param username user name
   * @param password user password
   * @return AuthToken containing access and refresh tokens
   */
  @Transactional
  public AuthToken login(String username, String password) {
    authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken.unauthenticated(username, password));

    var user = userService.findByUsername(username)
        .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));

    return authTokenService.createToken(user);
  }

  /**
   * Refresh access token using opaque refresh token with rotation
   *
   * @param refreshToken opaque refresh token
   * @return AuthToken with new access and refresh tokens
   */
  @Transactional
  public AuthToken refresh(String refreshToken) {
    var tokenEntity = refreshTokenService.rotate(refreshToken);
    var user = userService.requireEnabledUser(tokenEntity.getUserId());

    return authTokenService.createToken(user);
  }

  /**
   * Logout user by revoking all refresh tokens
   */
  @Transactional
  public void logout() {
    var userId = AuthUtils.requireCurrentUserId(UUID::fromString);
    refreshTokenService.revokeAll(userId);
  }

}
