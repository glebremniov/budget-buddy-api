package com.budget.buddy.budget_buddy_api.security.refresh.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.refresh-token")
public record RefreshTokenProperties(long validitySeconds, Cleanup cleanup) {

  record Cleanup(boolean enabled, String cron) {

  }
}
