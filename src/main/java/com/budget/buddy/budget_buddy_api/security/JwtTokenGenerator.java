package com.budget.buddy.budget_buddy_api.security;

import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

public class JwtTokenGenerator {

  public static String generate(String username, long validitySeconds, SecretKey key) {
    var now = new Date();
    var exp = new Date(now.getTime() + validitySeconds * 1000);

    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(username)
        .issuedAt(now)
        .expiration(exp)
        .signWith(key)
        .compact();
  }

}
