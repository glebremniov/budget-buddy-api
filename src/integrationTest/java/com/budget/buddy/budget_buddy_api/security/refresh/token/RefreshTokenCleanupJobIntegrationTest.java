package com.budget.buddy.budget_buddy_api.security.refresh.token;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "security.refresh-token.cleanup.enabled=true",
    "security.refresh-token.cleanup-cron=0 0 * * * *"
})
class RefreshTokenCleanupJobIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private ApplicationContext context;

  @Test
  void should_LoadCleanupJob_When_Enabled() {
    assertThat(context.containsBean("refreshTokenCleanupJob")).isTrue();
    assertThat(context.getBean(RefreshTokenCleanupJob.class)).isNotNull();
  }
}
