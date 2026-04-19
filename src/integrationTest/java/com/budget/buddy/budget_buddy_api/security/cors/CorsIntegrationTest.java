package com.budget.buddy.budget_buddy_api.security.cors;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CorsIntegrationTest extends BaseMvcIntegrationTest {

  private static final String ALLOWED_ORIGIN = "http://localhost:5173";
  private static final String DISALLOWED_ORIGIN = "https://evil.example.com";

  @Nested
  class PreflightTests {

    @Test
    void should_RespondWithCorsHeaders_When_PreflightFromAllowedOrigin() {
      var result = mvc.options().uri("/v1/categories")
          .header("Origin", ALLOWED_ORIGIN)
          .header("Access-Control-Request-Method", "POST")
          .exchange();

      assertThat(result)
          .as("preflight from allowed origin should succeed")
          .hasStatus(HttpStatus.OK);
      assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
          .as("allowed origin should be reflected in response")
          .isEqualTo(ALLOWED_ORIGIN);
      assertThat(result.getResponse().getHeader("Access-Control-Allow-Credentials"))
          .as("credentials should be allowed")
          .isEqualTo("true");
      assertThat(result.getResponse().getHeader("Access-Control-Allow-Methods"))
          .as("allowed methods should be present")
          .isNotBlank();
    }

    @Test
    void should_RejectPreflight_When_OriginNotAllowed() {
      var result = mvc.options().uri("/v1/categories")
          .header("Origin", DISALLOWED_ORIGIN)
          .header("Access-Control-Request-Method", "POST")
          .exchange();

      assertThat(result)
          .as("preflight from disallowed origin should be rejected")
          .hasStatus(HttpStatus.FORBIDDEN);
      assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
          .as("disallowed origin must not be reflected")
          .isNull();
    }

    @Test
    void should_RespondWithCorsHeaders_When_PreflightToAuthenticatedEndpoint() {
      var result = mvc.options().uri("/v1/categories")
          .header("Origin", ALLOWED_ORIGIN)
          .header("Access-Control-Request-Method", "GET")
          .exchange();

      assertThat(result)
          .as("preflight to authenticated endpoint from allowed origin should succeed")
          .hasStatus(HttpStatus.OK);
      assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
          .as("allowed origin should be reflected")
          .isEqualTo(ALLOWED_ORIGIN);
    }
  }

  @Nested
  class SimpleRequestTests {

    @Test
    void should_IncludeCorsHeader_When_SimpleRequestFromAllowedOrigin() {
      var result = mvc.get().uri("/actuator/health")
          .header("Origin", ALLOWED_ORIGIN)
          .exchange();

      assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
          .as("allowed origin should be reflected in simple request response")
          .isEqualTo(ALLOWED_ORIGIN);
    }

    @Test
    void should_RejectSimpleRequest_When_OriginNotAllowed() {
      var result = mvc.get().uri("/actuator/health")
          .header("Origin", DISALLOWED_ORIGIN)
          .exchange();

      assertThat(result)
          .as("simple request from disallowed origin should be rejected")
          .hasStatus(HttpStatus.FORBIDDEN);
      assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
          .as("disallowed origin must not be reflected")
          .isNull();
    }
  }
}
