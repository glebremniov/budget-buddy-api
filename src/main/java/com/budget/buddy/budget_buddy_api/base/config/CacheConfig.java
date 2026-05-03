package com.budget.buddy.budget_buddy_api.base.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  /** Maps {@code (issuer, subject) → localUserId} for the OIDC JIT-provisioning lookup. */
  public static final String LOCAL_USER_IDS = "localUserIds";

  /**
   * Caches JWKS responses from the OIDC issuer. Without this, {@code NimbusJwtDecoder} falls back
   * to {@code NoOpCache} and refetches the JWKS on every cycle, surfacing transient DNS failures
   * as 401s.
   */
  public static final String JWKS = "jwks";

  @Bean
  CacheManager cacheManager() {
    var cacheManager = new CaffeineCacheManager(LOCAL_USER_IDS, JWKS);
    cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)));
    return cacheManager;
  }
}
