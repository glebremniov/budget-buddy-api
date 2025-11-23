package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.model.AuthToken;
import com.budget.buddy.budget_buddy_api.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations. Handles user login and token refresh.
 */
@Service
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider tokenProvider;
  private final UserDetailsService userDetailsService;

  public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
    this.authenticationManager = authenticationManager;
    this.tokenProvider = tokenProvider;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Authenticate user with username and password
   *
   * @param username user name
   * @param password user password
   * @return AuthToken containing access and refresh tokens
   */
  public AuthToken login(String username, String password) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    var accessToken = tokenProvider.createAccessToken(username);
    var refreshToken = tokenProvider.createRefreshToken(username);
    var expiresIn = (int) tokenProvider.getAccessTokenValiditySeconds();
    return AuthTokenBuilder.build(accessToken, refreshToken, expiresIn);
  }

  /**
   * Refresh access token using refresh token
   *
   * @param refreshToken refresh token
   * @return AuthToken with new access token
   */
  public AuthToken refresh(String refreshToken) {
    if (!tokenProvider.validateToken(refreshToken)) {
      throw new JwtException("Invalid refresh token");
    }
    var username = tokenProvider.getUsername(refreshToken);
    userDetailsService.loadUserByUsername(username);
    var accessToken = tokenProvider.createAccessToken(username);
    var expiresIn = (int) tokenProvider.getAccessTokenValiditySeconds();
    return AuthTokenBuilder.build(accessToken, refreshToken, expiresIn);
  }
}
