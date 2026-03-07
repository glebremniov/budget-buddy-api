package com.budget.buddy.budget_buddy_api.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProperties.TokenProperties;
import java.time.Clock;
import java.time.Instant;

public class JwtProvider {

  private final Clock clock;
  private final Algorithm algorithm;
  private final JWTVerifier jwtVerifier;
  private final TokenProperties tokenProperties;

  public JwtProvider(Clock clock, TokenProperties tokenProperties) {
    this.clock = clock;
    this.algorithm = Algorithm.HMAC512(tokenProperties.secret());
    this.jwtVerifier = JWT.require(algorithm).build();
    this.tokenProperties = tokenProperties;
  }

  public String create(String username) {
    var now = Instant.now(clock);
    var expiresAt = now.plusSeconds(tokenProperties.validitySeconds());

    return JWT.create()
        .withSubject(username)
        .withIssuedAt(now)
        .withExpiresAt(expiresAt)
        .sign(algorithm);
  }

  public String getSubject(String token) {
    return jwtVerifier.verify(token)
        .getSubject();
  }

  public long getValiditySeconds() {
    return tokenProperties.validitySeconds();
  }

}
