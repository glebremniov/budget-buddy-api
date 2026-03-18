package com.budget.buddy.budget_buddy_api.security.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.generated.model.LoginRequest;
import com.budget.buddy.budget_buddy_api.generated.model.RefreshTokenRequest;
import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import com.budget.buddy.budget_buddy_api.security.refresh.token.RefreshTokenRepository;
import java.time.OffsetDateTime;
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
  RefreshTokenRepository refreshTokenRepository;

  // ── helpers ────────────────────────────────────────────────────────────────

  AuthToken refresh(String refreshToken) throws Exception {
    var exchange = mvc.post().uri("/v1/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new RefreshTokenRequest().refreshToken(refreshToken)))
        .exchange();

    assertThat(exchange).hasStatus(HttpStatus.OK);

    return objectMapper.readValue(
        exchange.getResponse().getContentAsString(), AuthToken.class);
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class Register {

    @Test
    void should_RegisterUser_When_ValidRequest() {
      assertThat(mvc.post().uri("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new RegisterRequest().username("newuser").password("password123"))))
          .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void should_Return409_When_UsernameAlreadyTaken() {
      register(USERNAME, PASSWORD);

      assertThat(mvc.post().uri("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new RegisterRequest().username(USERNAME).password(PASSWORD))))
          .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void should_Return400_When_PasswordTooShort() {
      assertThat(mvc.post().uri("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new RegisterRequest().username("newuser").password("short"))))
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_UsernameTooShort() {
      assertThat(mvc.post().uri("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new RegisterRequest().username("ab").password("password123"))))
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  class Login {

    @Test
    void should_ReturnTokens_When_ValidCredentials() throws Exception {
      register(USERNAME, PASSWORD);

      var token = login(USERNAME, PASSWORD);

      assertThat(token.getAccessToken()).isNotEmpty();
      assertThat(token.getRefreshToken()).isNotEmpty();
      assertThat(token.getTokenType()).isEqualTo("Bearer");
      assertThat(token.getExpiresIn()).isPositive();
    }

    @Test
    void should_Return401_When_InvalidPassword() {
      register(USERNAME, PASSWORD);

      assertThat(mvc.post().uri("/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new LoginRequest().username(USERNAME).password("wrongpassword"))))
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_Return401_When_UserNotFound() {
      assertThat(mvc.post().uri("/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new LoginRequest().username("nonexistent").password(PASSWORD))))
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_PersistRefreshToken_When_LoginSuccessful() throws Exception {
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);

      assertThat(refreshTokenRepository.findValidToken(
          token.getRefreshToken(), OffsetDateTime.now())).isPresent();
    }
  }

  @Nested
  class Refresh {

    @Test
    void should_ReturnNewTokens_When_ValidRefreshToken() throws Exception {
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);

      var newToken = refresh(token.getRefreshToken());

      assertThat(newToken.getAccessToken()).isNotEmpty();
      assertThat(newToken.getRefreshToken()).isNotEmpty();
    }

    @Test
    void should_RotateRefreshToken_When_Refreshed() throws Exception {
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);
      var oldRefreshToken = token.getRefreshToken();

      var newToken = refresh(oldRefreshToken);

      assertThat(refreshTokenRepository.findValidToken(
          oldRefreshToken, OffsetDateTime.now())).isEmpty();

      assertThat(refreshTokenRepository.findValidToken(
          newToken.getRefreshToken(), OffsetDateTime.now())).isPresent();
    }

    @Test
    void should_Return401_When_InvalidRefreshToken() {
      assertThat(mvc.post().uri("/v1/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new RefreshTokenRequest().refreshToken("invalid-token"))))
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_Return401_When_RefreshTokenUsedTwice() throws Exception {
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);
      var refreshToken = token.getRefreshToken();

      refresh(refreshToken);

      assertThat(mvc.post().uri("/v1/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new RefreshTokenRequest().refreshToken(refreshToken))))
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Logout {

    @Test
    void should_Return204_When_LoggedOut() throws Exception {
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);

      assertThat(mvc.post().uri("/v1/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken()))
          .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_InvalidateRefreshToken_When_LoggedOut() throws Exception {
      register(USERNAME, PASSWORD);
      var token = login(USERNAME, PASSWORD);

      assertThat(mvc.post().uri("/v1/auth/logout")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken()))
          .hasStatus(HttpStatus.NO_CONTENT);

      assertThat(mvc.post().uri("/v1/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new RefreshTokenRequest().refreshToken(token.getRefreshToken()))))
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      assertThat(mvc.post().uri("/v1/auth/logout"))
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
