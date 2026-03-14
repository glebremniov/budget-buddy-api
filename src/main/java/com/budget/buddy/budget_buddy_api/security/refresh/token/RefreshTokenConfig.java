package com.budget.buddy.budget_buddy_api.security.refresh.token;

import java.util.UUID;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(RefreshTokenProperties.class)
public class RefreshTokenConfig {

  @Bean
  RefreshTokenProvider uuidRefreshTokenProvider() {
    return () -> UUID.randomUUID().toString();
  }

}
