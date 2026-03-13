package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProperties;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProvider;
import com.budget.buddy.budget_buddy_api.user.UserEntity;
import com.budget.buddy.budget_buddy_api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations. Handles user login and token refresh.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private static final String TOKEN_TYPE = "Bearer";

  private final AuthenticationManager authenticationManager;
  private final JwtProperties tokenProperties;
  private final JwtProvider tokenProvider;
  private final UserRepository userRepository;
  private final JwtDecoder jwtDecoder;

  private static AuthToken buildAuthToken(String accessToken, String refreshToken, int expiresInSeconds) {
    var token = new AuthToken();
    token.setAccessToken(accessToken);
    token.setRefreshToken(refreshToken);
    token.setTokenType(TOKEN_TYPE);
    token.setExpiresIn(expiresInSeconds);

    return token;
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

    var user = userRepository.findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    var subject = user.getId().toString();
    var accessToken = tokenProvider.create(subject, tokenProperties.accessTokenValiditySeconds());
    var refreshToken = tokenProvider.create(subject, tokenProperties.refreshTokenValiditySeconds());
    var expiresIn = (int) tokenProperties.accessTokenValiditySeconds();

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

    var accessToken = tokenProvider
        .create(user.getId().toString(), tokenProperties.accessTokenValiditySeconds());
    var expiresIn = (int) tokenProperties.accessTokenValiditySeconds();

    return buildAuthToken(accessToken, refreshToken, expiresIn);
  }

  public UserEntity requireEnabledUser(String subject) {
    var userId = AuthUtils.toUserId(subject);
    var user = userRepository.findById(userId)
        .orElseThrow(() -> new BadCredentialsException("User with id %s is not found".formatted(userId)));

    if (user.isEnabled()) {
      return user;
    }

    throw new DisabledException("User is disabled");
  }
}
