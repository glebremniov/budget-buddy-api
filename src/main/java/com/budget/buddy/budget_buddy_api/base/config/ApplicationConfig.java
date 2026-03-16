package com.budget.buddy.budget_buddy_api.base.config;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  Supplier<UUID> idGenerator() {
    return UUID::randomUUID;
  }

}
