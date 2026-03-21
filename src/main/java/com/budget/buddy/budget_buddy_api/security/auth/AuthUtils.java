package com.budget.buddy.budget_buddy_api.security.auth;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

/**
 * Utility class for security and authentication related operations.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthUtils {

  private static Optional<Jwt> toJwt(Object principle) {
    if (principle instanceof Jwt jwt) {
      return Optional.of(jwt);
    }
    return Optional.empty();
  }

  /**
   * Retrieves the current authenticated user's ID using the provided converter.
   *
   * @param converter the converter to transform the subject (string) to the target type
   * @param <T> the target type of the user ID
   * @return the current user ID
   * @throws InvalidBearerTokenException if the user is not authenticated
   */
  public static <T> T requireCurrentUserId(Converter<String, T> converter) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    return Optional.ofNullable(authentication)
        .map(Authentication::getPrincipal)
        .flatMap(AuthUtils::toJwt)
        .map(JwtClaimAccessor::getSubject)
        .map(converter::convert)
        .orElseThrow(() -> new InvalidBearerTokenException("Current user is not authenticated."));
  }

}
