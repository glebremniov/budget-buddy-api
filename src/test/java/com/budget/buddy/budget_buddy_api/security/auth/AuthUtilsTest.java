package com.budget.buddy.budget_buddy_api.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

@DisplayName("AuthUtils Unit Tests")
class AuthUtilsTest {

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_ReturnUserId_When_AuthenticatedWithJwt() {
    // Given
    var userId = UUID.randomUUID();
    var userIdString = userId.toString();
    var jwt = mock(Jwt.class);
    when(jwt.getSubject()).thenReturn(userIdString);

    var authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(jwt);

    var securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    // When
    var actual = AuthUtils.requireCurrentUserId(UUID::fromString);

    // Then
    assertThat(actual)
        .as("Converted user ID should match the JWT subject")
        .isEqualTo(userId);
  }

  @Test
  void should_ThrowException_When_NoAuthentication() {
    // Given
    SecurityContextHolder.clearContext();

    // When & Then
    assertThatThrownBy(() -> AuthUtils.requireCurrentUserId(UUID::fromString))
        .as("Should throw InvalidBearerTokenException when no authentication is present in the context")
        .isInstanceOf(InvalidBearerTokenException.class)
        .hasMessage("Current user is not authenticated.");
  }

  @Test
  void should_ThrowException_When_PrincipalIsNotJwt() {
    // Given
    var authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn("not-a-jwt");

    var securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    // When & Then
    assertThatThrownBy(() -> AuthUtils.requireCurrentUserId(UUID::fromString))
        .as("Should throw InvalidBearerTokenException when authentication principal is not a JWT")
        .isInstanceOf(InvalidBearerTokenException.class)
        .hasMessage("Current user is not authenticated.");
  }

  @Test
  void should_ThrowException_When_JwtHasNoSubject() {
    // Given
    var jwt = mock(Jwt.class);
    when(jwt.getSubject()).thenReturn(null);

    var authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(jwt);

    var securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    // When & Then
    assertThatThrownBy(() -> AuthUtils.requireCurrentUserId(UUID::fromString))
        .as("Should throw InvalidBearerTokenException when JWT has no subject claim")
        .isInstanceOf(InvalidBearerTokenException.class)
        .hasMessage("Current user is not authenticated.");
  }
}
