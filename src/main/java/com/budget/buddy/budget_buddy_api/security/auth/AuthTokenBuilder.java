package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.model.AuthToken;
import org.springframework.stereotype.Component;

@Component
public final class AuthTokenBuilder {

  private static final String TOKEN_TYPE = "Bearer";

  public static AuthToken build(String accessToken, String refreshToken, int expiresInSeconds) {
    var token = new AuthToken();

    token.setAccessToken(accessToken);
    token.setRefreshToken(refreshToken);
    token.setTokenType(TOKEN_TYPE);
    token.setExpiresIn(expiresInSeconds);

    return token;
  }

}
