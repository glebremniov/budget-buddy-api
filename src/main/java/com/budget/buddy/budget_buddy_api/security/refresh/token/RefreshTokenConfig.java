package com.budget.buddy.budget_buddy_api.security.refresh.token;

import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RefreshTokenConfig {

  @Bean
  RefreshTokenProvider uuidRefreshTokenProvider() {
    return () -> UUID.randomUUID().toString();
  }

}
