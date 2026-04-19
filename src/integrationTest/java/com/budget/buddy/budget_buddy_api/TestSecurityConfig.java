package com.budget.buddy.budget_buddy_api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Provides a local JwtDecoder for integration tests so that Spring Boot does not
 * attempt to contact the OIDC issuer-uri on startup.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestSecurityConfig {

  private static final String TEST_SECRET = "integration-test-secret-key-that-is-at-least-32-chars";

  @Bean
  JwtDecoder jwtDecoder() {
    var key = new SecretKeySpec(TEST_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(key).build();
  }
}
