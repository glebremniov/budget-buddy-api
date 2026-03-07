package com.budget.buddy.budget_buddy_api.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProperties.TokenProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class JwtProviderTest {

  private static final String EXPECTED_TOKEN = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9."
      + "eyJzdWIiOiJ0ZXN0LXVzZXIiLCJpYXQiOjExOTY2NzY5MzAsImV4cCI6MTE5NjY3ODEzMH0."
      + "b0HpQ1DpUKI59TzwEHexR8gCznVhKrBLw9qsk0XoX68ZpOyXdEUYmR_8FUE60aYqtQdkVOhZ8VYe4axdfJTk8A";
  private static final String TOKEN_SUBJECT = "test-user";
  private static final String TOKEN_SECRET = "secret";
  private static final long TOKEN_VALIDITY_SECONDS = 1200L;
  private static final Instant NOW = Instant.parse("2007-12-03T10:15:30.00Z");
  private static final TokenProperties TOKEN_PROPERTIES = new TokenProperties(TOKEN_SECRET, TOKEN_VALIDITY_SECONDS);
  private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private final JwtProvider provider = new JwtProvider(FIXED_CLOCK, TOKEN_PROPERTIES);

  @Test
  void create_Should_ReturnValidToken() {
    // When
    var accessToken = provider.create(TOKEN_SUBJECT);

    // Then
    assertThat(accessToken).isEqualTo(EXPECTED_TOKEN);
  }

  @Disabled("To figure out how to mock clock in the library")
  @Test
  void getSubject_Should_ReturnParsedSubject() {
    // When
    var parsed = provider.getSubject(EXPECTED_TOKEN);

    // Then
    assertThat(parsed).isEqualTo(TOKEN_SUBJECT);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "invalid")
  void getSubject_Should_ThrowException_When_InvalidToken(String invalidToken) {
    assertThatThrownBy(() -> provider.getSubject(invalidToken))
        .isInstanceOf(JWTDecodeException.class);
  }

  @Test
  void getValiditySeconds_Should_ReturnValiditySecondsFromProperties() {
    // When
    var actual = provider.getValiditySeconds();

    // Then
    assertThat(actual).isEqualTo(TOKEN_VALIDITY_SECONDS);
  }
}
