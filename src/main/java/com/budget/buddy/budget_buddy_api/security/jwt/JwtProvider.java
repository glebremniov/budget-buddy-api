package com.budget.buddy.budget_buddy_api.security.jwt;

import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

  private final Clock clock;
  private final JwtEncoder jwtEncoder;

  public String create(String subject, long validitySeconds) {
    var claims = buildClaims(subject, validitySeconds);

    return jwtEncoder
        .encode(JwtEncoderParameters.from(claims))
        .getTokenValue();
  }

  private JwtClaimsSet buildClaims(String subject, long validitySeconds) {
    var now = Instant.now(clock);
    var expiresAt = now.plusSeconds(validitySeconds);

    return JwtClaimsSet.builder()
        .subject(subject)
        .issuedAt(now)
        .expiresAt(expiresAt)
        .build();
  }

}
