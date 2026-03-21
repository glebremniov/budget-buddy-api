package com.budget.buddy.budget_buddy_api.security.refresh.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "security.refresh-token.cleanup",
    name = "enabled",
    havingValue = "true"
)
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

  private final RefreshTokenService refreshTokenService;

  @Scheduled(cron = "${security.refresh-token.cleanup.cron}")
  public void cleanupExpiredTokens() {
    log.info("Starting expired refresh tokens cleanup");
    refreshTokenService.deleteExpired();
    log.info("Expired refresh tokens cleanup completed");
  }
}
