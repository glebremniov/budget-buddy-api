package com.budget.buddy.budget_buddy_api.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

class JwtConfigTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(JwtConfig.class)
      .withPropertyValues("security.access-token.validity-seconds=900");

  @Nested
  class InvalidSecretTests {

    @ParameterizedTest
    @ValueSource(strings = {"short", "1234567890123456789012345678901"})
    void should_FailToStart_When_SecretIsTooShort(String secret) {
      contextRunner
          .withPropertyValues("security.access-token.secret=" + secret)
          .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void should_FailToStart_When_SecretIsBlank() {
      contextRunner
          .withPropertyValues("security.access-token.secret=" + " ".repeat(32))
          .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void should_FailToStart_When_SecretIsNotConfigured() {
      contextRunner
          .run(context -> assertThat(context).hasFailed());
    }
  }

  @Nested
  class ValidSecretTests {

    @Test
    void should_StartSuccessfully_When_SecretIsAtLeast32Characters() {
      contextRunner
          .withPropertyValues("security.access-token.secret=12345678901234567890123456789012")
          .run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(JwtDecoder.class);
            assertThat(context).hasSingleBean(JwtEncoder.class);
          });
    }
  }
}
