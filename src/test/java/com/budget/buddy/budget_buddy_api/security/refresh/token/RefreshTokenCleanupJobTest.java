package com.budget.buddy.budget_buddy_api.security.refresh.token;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupJobTest {

  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private RefreshTokenCleanupJob refreshTokenCleanupJob;

  @Test
  void should_CallDeleteExpired_When_CleanupJobRuns() {
    // When
    refreshTokenCleanupJob.cleanupExpiredTokens();

    // Then
    verify(refreshTokenService).deleteExpired();
  }
}
