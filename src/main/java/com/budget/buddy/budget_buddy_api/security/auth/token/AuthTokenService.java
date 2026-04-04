package com.budget.buddy.budget_buddy_api.security.auth.token;

import com.budget.buddy.budget_buddy_contracts.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.security.TokenService;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProperties;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProvider;
import com.budget.buddy.budget_buddy_api.security.refresh.token.RefreshTokenService;
import com.budget.buddy.budget_buddy_api.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for creating complex authentication tokens (access + refresh).
 */
@Service
@RequiredArgsConstructor
public class AuthTokenService implements TokenService<AuthToken> {

  private static final String TOKEN_TYPE = "Bearer";

  private final JwtProperties jwtProperties;
  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;

  private static AuthToken buildAuthToken(String accessToken, String refreshToken, int expiresInSeconds) {
    var token = new AuthToken();
    token.setAccessToken(accessToken);
    token.setRefreshToken(refreshToken);
    token.setTokenType(TOKEN_TYPE);
    token.setExpiresIn(expiresInSeconds);
    return token;
  }

  @Override
  public AuthToken createToken(UserDto user) {
    var accessToken = jwtProvider.create(user.id().toString(), jwtProperties.validitySeconds());
    var refreshToken = refreshTokenService.createToken(user);
    var expiresIn = (int) jwtProperties.validitySeconds();

    return buildAuthToken(accessToken, refreshToken, expiresIn);
  }

}
