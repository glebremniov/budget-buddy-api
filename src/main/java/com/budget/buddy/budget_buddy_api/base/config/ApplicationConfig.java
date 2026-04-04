package com.budget.buddy.budget_buddy_api.base.config;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_api.security.auth.AuthUtils;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  Supplier<UUID> idGenerator() {
    return UUID::randomUUID;
  }

  /**
   * Provides the ID of the currently authenticated user by reading the JWT subject from the
   * active security context.
   *
   * @return an {@link OwnerIdProvider} backed by the current request's security context
   */
  @Bean
  OwnerIdProvider<UUID> ownerIdProvider() {
    return () -> AuthUtils.requireCurrentUserId(UUID::fromString);
  }

}
