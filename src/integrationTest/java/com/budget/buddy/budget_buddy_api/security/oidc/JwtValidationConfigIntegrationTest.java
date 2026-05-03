package com.budget.buddy.budget_buddy_api.security.oidc;

import com.budget.buddy.budget_buddy_api.BaseIntegrationTest;
import com.budget.buddy.budget_buddy_api.base.config.CacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.JwkSetUriJwtDecoderBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that JWT validation properties are correctly loaded.
 * <p>
 * Note: MockMvc's {@code jwt()} post-processor bypasses the real {@code JwtDecoder},
 * so audience/issuer rejection cannot be tested at the HTTP level with mock JWTs.
 * These tests verify the configuration is present so that Spring Boot's auto-configured
 * audience validator is active at runtime.
 */
@DisplayName("JWT Validation Configuration Tests")
class JwtValidationConfigIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private Environment environment;

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private JwkSetUriJwtDecoderBuilderCustomizer jwkSetUriJwtDecoderBuilderCustomizer;

  @Test
  @DisplayName("should have audience validation configured")
  void shouldHaveAudienceValidationConfigured() {
    var audience = environment.getProperty("spring.security.oauth2.resourceserver.jwt.audiences[0]");
    assertThat(audience)
        .as("audiences property must be set for Spring Boot to auto-configure audience validation")
        .isEqualTo("budget-buddy-api");
  }

  @Test
  @DisplayName("should have issuer URI configured")
  void shouldHaveIssuerUriConfigured() {
    var issuerUri = environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
    assertThat(issuerUri)
        .as("issuer-uri must be set for JWT signature and issuer validation")
        .isNotBlank();
  }

  @Test
  @DisplayName("should register the jwks cache so NimbusJwtDecoder can avoid NoOpCache")
  void shouldRegisterJwksCache() {
    assertThat(cacheManager.getCache(CacheConfig.JWKS))
        .as("jwks cache must be registered so the JWT decoder can cache the JWKS response")
        .isNotNull();
  }

  @Test
  @DisplayName("should expose a JwkSetUriJwtDecoderBuilderCustomizer that attaches the jwks cache")
  void shouldExposeJwkSetUriJwtDecoderBuilderCustomizer() {
    assertThat(jwkSetUriJwtDecoderBuilderCustomizer)
        .as("the cache customizer bean must be present so Spring Boot applies it when building the NimbusJwtDecoder")
        .isNotNull();
  }
}
