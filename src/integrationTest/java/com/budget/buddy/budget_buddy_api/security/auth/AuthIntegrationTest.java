package com.budget.buddy.budget_buddy_api.security.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.generated.model.LoginRequest;
import com.budget.buddy.budget_buddy_api.generated.model.RefreshTokenRequest;
import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import com.budget.buddy.budget_buddy_api.security.refresh.token.TestRefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends BaseMvcIntegrationTest {

  static final String USERNAME = "testuser";
  static final String PASSWORD = "testpassword123";

  @Autowired
  TestRefreshTokenRepository refreshTokenRepository;

  // ── helpers ────────────────────────────────────────────────────────────────

  private static String sha256(String input) {
    try {
      return HexFormat.of().formatHex(
          MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  AuthToken refresh(String refreshToken) throws Exception {
    var exchange = mvc.post().uri("/v1/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new RefreshTokenRequest().refreshToken(refreshToken)))
        .exchange();

    assertThat(exchange)
        .as("Refresh request should be successful")
        .hasStatus(HttpStatus.OK);

    return objectMapper.readValue(
        exchange.getResponse().getContentAsString(), AuthToken.class);
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class Register {

    @Test
    void should_RegisterUser_When_ValidRequest() {
      // Given
      var request = new RegisterRequest().username("newuser").password("password123");

      // When
      var exchange = mvc.post().uri("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(request))
          .exchange();

      // Then
      assertThat(exchange)
          .as("Registration should return 201 Created")
          .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void should_Return409_When_UsernameAlreadyTaken() {
      // Given
      register(USERNAME, PASSWORD);
      var request = new RegisterRequest().username(USERNAME).password(PASSWORD);

      // When
      var exchange = mvc.post().uri("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(request))
          .exchange();

      // Then
      assertThat(exchange)
          .as("Registration with existing username should return 409 Conflict")
          .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void should_Return400_When_PasswordTooShort() {
      // Given
      var request = new RegisterRequest().username("newuser").password("short");

      // When
      var exchange = mvc.post().uri("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(request))
          .exchange();

      // Then
      assertThat(exchange)
          .as("Registration with short password should return 400 Bad Request")
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_UsernameTooShort() {
      // Given
      var request = new RegisterRequest().username("ab").password("password123");

      // When
      var exchange = mvc.post().uri("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(request))
          .exchange();

      // Then
      assertThat(exchange)
          .as("Registration with short username should return 400 Bad Request")
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  class Login {

    @Test
    void should_ReturnTokens_When_ValidCredentials() throws Exception {
      // Given
      register(USERNAME, PASSWORD);

      // When
      var token = login(USERNAME, PASSWORD);

      // Then
      assertThat(token)
          .as("Login should return valid token type")
          .returns("Bearer", AuthToken::getTokenType);
      assertThat(token.getAccessToken())
          .as("Access token should not be empty")
          .isNotEmpty();
      assertThat(token.getRefreshToken())
          .as("Refresh token should not be empty")
          .isNotEmpty();
      assertThat(token.getExpiresIn())
          .as("Expires in should be positive")
          .isPositive();
    }

    @Test
    void should_Return401_When_InvalidPassword() {
      // Given
      register(USERNAME, PASSWORD);
      var request = new LoginRequest()
          .username(USERNAME)
          .password("wrongpassword");

      // When
      var exchange = mvc.post().uri("/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(request))
          .exchange();

      // Then
      assertThat(exchange)
          .as("Login with wrong password should return 401 Unauthorized")
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_Return401_When_UserNotFound() {
      // Given
      var request = new LoginRequest()
          .username("nonexistent")
          .password(PASSWORD);

      // When
      var exchange = mvc.post().uri("/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(request))
          .exchange();

      // Then
      assertThat(exchange)
          .as("Login with non-existent user should return 401 Unauthorized")
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_PersistRefreshToken_When_LoginSuccessful() throws Exception {
      // Given
      register(USERNAME, PASSWORD);

      // When
      var token = login(USERNAME, PASSWORD);
      var rawToken = token.getRefreshToken();

      // Then
      var stored = refreshTokenRepository.findValidToken(sha256(rawToken), OffsetDateTime.now());
      assertThat(stored)
          .as("Refresh token hash should be persisted in database")
          .isPresent();
      assertThat(stored.get().getTokenHash())
          .as("Stored value must be the hash, not the raw token")
          .isNotEqualTo(rawToken)
          .hasSize(64);
    }
  }

  @Nested
  class Refresh {

    @Test
    void should_ReturnNewTokens_When_ValidRefreshToken() throws Exception {
      // Given
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);

      // When
      var newToken = refresh(token.getRefreshToken());

      // Then
      assertThat(newToken)
          .as("Refresh should return new valid tokens")
          .satisfies(t -> {
            assertThat(t.getAccessToken()).as("New access token should not be empty").isNotEmpty();
            assertThat(t.getRefreshToken()).as("New refresh token should not be empty").isNotEmpty();
          });
    }

    @Test
    void should_RotateRefreshToken_When_Refreshed() throws Exception {
      // Given
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);
      var oldRefreshToken = token.getRefreshToken();

      // When
      var newToken = refresh(oldRefreshToken);

      // Then
      assertThat(refreshTokenRepository.findValidToken(sha256(oldRefreshToken), OffsetDateTime.now()))
          .as("Old refresh token should be invalidated")
          .isEmpty();

      assertThat(refreshTokenRepository.findValidToken(sha256(newToken.getRefreshToken()), OffsetDateTime.now()))
          .as("New refresh token should be persisted")
          .isPresent();
    }

    @Test
    void should_Return401_When_InvalidRefreshToken() {
      // Given
      var request = new RefreshTokenRequest()
          .refreshToken("invalid-token");

      // When
      var exchange = mvc.post().uri("/v1/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(request))
          .exchange();

      // Then
      assertThat(exchange)
          .as("Refresh with invalid token should return 401 Unauthorized")
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_Return401_When_RefreshTokenUsedTwice() throws Exception {
      // Given
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);
      var refreshToken = token.getRefreshToken();
      refresh(refreshToken);

      // When
      var exchange = mvc.post().uri("/v1/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new RefreshTokenRequest().refreshToken(refreshToken)))
          .exchange();

      // Then
      assertThat(exchange)
          .as("Using same refresh token twice should return 401 Unauthorized")
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Logout {

    @Test
    void should_Return204_When_LoggedOut() throws Exception {
      // Given
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);

      // When
      var exchange = mvc.post().uri("/v1/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
          .exchange();

      // Then
      assertThat(exchange)
          .as("Logout should return 204 No Content")
          .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_InvalidateRefreshToken_When_LoggedOut() throws Exception {
      // Given
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);
      mvc.post().uri("/v1/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
          .exchange();

      // When
      var exchange = mvc.post().uri("/v1/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new RefreshTokenRequest().refreshToken(token.getRefreshToken())))
          .exchange();

      // Then
      assertThat(exchange)
          .as("Refresh after logout should return 401 Unauthorized")
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      // When
      var exchange = mvc.post().uri("/v1/auth/logout")
          .exchange();

      // Then
      assertThat(exchange)
          .as("Logout without authentication should return 401 Unauthorized")
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
