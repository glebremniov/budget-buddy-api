package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProperties;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProvider;
import com.budget.buddy.budget_buddy_api.user.UserDto;
import com.budget.buddy.budget_buddy_api.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations.
 * Handles user registration, login and token refresh.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private static final String TOKEN_TYPE = "Bearer";

  private final JwtProperties jwtProperties;
  private final JwtProvider jwtProvider;
  private final JwtDecoder jwtDecoder;
  private final AuthenticationManager authenticationManager;
  private final UserService userService;

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
   * Authenticate user with username and password
   *
   * @param username user name
   * @param password user password
   * @return AuthToken containing access and refresh tokens
   */
  public AuthToken login(String username, String password) {
    var authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
    authenticationManager.authenticate(authenticationRequest);

    var user = userService.findByUsername(username)
        .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));

    var subject = user.id().toString();
    var accessToken = jwtProvider.create(subject, jwtProperties.accessTokenValiditySeconds());
    var refreshToken = jwtProvider.create(subject, jwtProperties.refreshTokenValiditySeconds());
    var expiresIn = (int) jwtProperties.accessTokenValiditySeconds();

    return buildAuthToken(accessToken, refreshToken, expiresIn);
  }

  /**
   * Refresh access token using refresh token
   *
   * @param refreshToken refresh token
   * @return AuthToken with new access token
   */
  public AuthToken refresh(String refreshToken) {
    var decoded = jwtDecoder.decode(refreshToken);
    var user = requireEnabledUser(decoded.getSubject());

    var accessToken = jwtProvider
        .create(user.id().toString(), jwtProperties.accessTokenValiditySeconds());
    var expiresIn = (int) jwtProperties.accessTokenValiditySeconds();

    return buildAuthToken(accessToken, refreshToken, expiresIn);
  }

  public UserDto requireEnabledUser(String subject) {
    var userId = AuthUtils.toUserId(subject);
    var user = userService.read(userId);

    if (user.enabled()) {
      return user;
    }

    throw new DisabledException("User is disabled");
  }
}
