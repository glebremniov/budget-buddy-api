package com.budget.buddy.budget_buddy_api.security.auth;

import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthUtils {

  private static Optional<Jwt> toJwt(Object principle) {
    if (principle instanceof Jwt jwt) {
      return Optional.of(jwt);
    }
    return Optional.empty();
  }

  public static UUID toUserId(String subject) {
    try {
      return UUID.fromString(subject);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid subjet claim: {}. Should be valid UUID", subject, e);
      throw new InvalidBearerTokenException("Invalid Bearer Token");
    }
  }

  public static UUID requireCurrentUserId() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    return Optional.ofNullable(authentication)
        .map(Authentication::getPrincipal)
        .flatMap(AuthUtils::toJwt)
        .map(JwtClaimAccessor::getSubject)
        .map(AuthUtils::toUserId)
        .orElseThrow(() -> new InvalidBearerTokenException("Current user is not authenticated."));
  }

}
