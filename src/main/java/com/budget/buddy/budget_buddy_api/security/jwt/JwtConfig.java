package com.budget.buddy.budget_buddy_api.security.jwt;

import static org.springframework.security.oauth2.jose.jws.JwsAlgorithms.HS256;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

  private static SecretKey buildSecretKey(String secret) {
    return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HS256);
  }

  @Bean
  JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
    return NimbusJwtDecoder
        .withSecretKey(buildSecretKey(jwtProperties.secret()))
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
  }

  @Bean
  JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
    return NimbusJwtEncoder
        .withSecretKey(buildSecretKey(jwtProperties.secret()))
        .algorithm(MacAlgorithm.HS256)
        .build();
  }
}
