package com.budget.buddy.budget_buddy_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

  private final SecretKey key;
  private final JwtProperties jwtProperties;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    this.jwtProperties = jwtProperties;
  }

  public String createAccessToken(String username) {
    return JwtTokenGenerator.generate(username, jwtProperties.accessTokenValiditySeconds(), key);
  }

  public String createRefreshToken(String username) {
    return JwtTokenGenerator.generate(username, jwtProperties.refreshTokenValiditySeconds(), key);
  }

  public Jws<Claims> parseToken(String token) throws JwtException {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token);
  }

  public boolean validateToken(String token) {
    try {
      parseToken(token);
      return true;
    } catch (JwtException ex) {
      log.error("Invalid JWT Token", ex);
      return false;
    }
  }

  public String getUsername(String token) {
    return parseToken(token).getPayload().getSubject();
  }

  public long getAccessTokenValiditySeconds() {
    return this.jwtProperties.accessTokenValiditySeconds();
  }

}
