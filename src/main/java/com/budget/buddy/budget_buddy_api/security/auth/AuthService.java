package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProperties;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProvider;
import com.budget.buddy.budget_buddy_api.security.refresh.token.RefreshTokenService;
import com.budget.buddy.budget_buddy_api.user.UserDto;
import com.budget.buddy.budget_buddy_api.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations.
 * Handles user registration, login, token refresh and logout.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private static final String TOKEN_TYPE = "Bearer";

  private final JwtProperties jwtProperties;
  private final JwtProvider jwtProvider;
  private final AuthenticationManager authenticationManager;
  private final UserService userService;
  private final RefreshTokenService refreshTokenService;

  private static AuthToken buildAuthToken(String accessToken, String refreshToken, int expiresInSeconds) {
    var token = new AuthToken();
    token.setAccessToken(accessToken);
    token.setRefreshToken(refreshToken);
    token.setTokenType(TOKEN_TYPE);
    token.setExpiresIn(expiresInSeconds);
    return token;
  }

  /**
   * Register a new user with default role
   *
   * @param request registration request containing username and password
   * @throws DataIntegrityViolationException if username is already taken
   */
  @Transactional
  public void register(RegisterRequest request) {
    if (userService.existsByUsername(request.getUsername())) {
      throw new DataIntegrityViolationException("Username already taken: " + request.getUsername());
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

    var accessToken = jwtProvider.create(user.id().toString(), jwtProperties.accessTokenValiditySeconds());
    var refreshToken = refreshTokenService.create(user.id(), jwtProperties.refreshTokenValiditySeconds());
    var expiresIn = (int) jwtProperties.accessTokenValiditySeconds();

    return buildAuthToken(accessToken, refreshToken, expiresIn);
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
    var user = requireEnabledUser(tokenEntity.getUserId());

    var newAccessToken = jwtProvider.create(user.id().toString(), jwtProperties.accessTokenValiditySeconds());
    var newRefreshToken = refreshTokenService.create(user.id(), jwtProperties.refreshTokenValiditySeconds());
    var expiresIn = (int) jwtProperties.accessTokenValiditySeconds();

    return buildAuthToken(newAccessToken, newRefreshToken, expiresIn);
  }

  /**
   * Logout user by revoking all refresh tokens
   */
  @Transactional
  public void logout() {
    var userId = AuthUtils.requireCurrentUserId();
    refreshTokenService.revokeAll(userId);
  }

  /**
   * Find and validate that user exists and is enabled
   *
   * @param userId user ID
   * @return UserDto if user exists and is enabled
   * @throws DisabledException if user is disabled
   */
  public UserDto requireEnabledUser(java.util.UUID userId) {
    var user = userService.read(userId);

    if (!user.enabled()) {
      throw new DisabledException("User is disabled");
    }

    return user;
  }

}
