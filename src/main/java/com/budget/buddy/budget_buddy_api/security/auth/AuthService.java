package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProperties;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProvider;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
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
  private final UserDetailsService userDetailsService;
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
    var authenticationRequest = UsernamePasswordAuthenticationToken
        .unauthenticated(username, password);
    authenticationManager.authenticate(authenticationRequest);

    var accessToken = tokenProvider.create(username, tokenProperties.accessTokenValiditySeconds());
    var refreshToken = tokenProvider.create(username, tokenProperties.refreshTokenValiditySeconds());
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
    var username = decoded.getSubject();
    userDetailsService.loadUserByUsername(username);

    var accessToken = tokenProvider.create(username, tokenProperties.accessTokenValiditySeconds());
    var expiresIn = (int) tokenProperties.accessTokenValiditySeconds();

    return buildAuthToken(accessToken, refreshToken, expiresIn);
  }

  /**
   * Get the username of the currently authenticated user
   *
   * @return Optional containing username if authenticated, empty otherwise
   */
  public Optional<String> getCurrentUserName() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    return Optional.ofNullable(authentication)
        .filter(Predicate.not(AnonymousAuthenticationToken.class::isInstance))
        .map(Authentication::getName);
  }

}
