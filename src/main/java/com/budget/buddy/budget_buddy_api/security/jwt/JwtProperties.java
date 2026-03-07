package com.budget.buddy.budget_buddy_api.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(TokenProperties accessToken, TokenProperties refreshToken) {


  public record TokenProperties(String secret, long validitySeconds) {

  }
}
