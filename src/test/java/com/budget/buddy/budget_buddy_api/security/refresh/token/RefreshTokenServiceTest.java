package com.budget.buddy.budget_buddy_api.security.refresh.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.user.UserDto;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  private static final Instant NOW = Instant.parse("2026-03-21T08:00:00Z");
  private static final long VALIDITY_SECONDS = 3600L;

  private final Clock clock = Clock.fixed(NOW, ZoneId.of("UTC"));

  @Mock
  private RefreshTokenProvider tokenProvider;
  @Mock
  private RefreshTokenRepository repository;
  @Mock
  private RefreshTokenProperties properties;

  private RefreshTokenService refreshTokenService;

  @BeforeEach
  void setUp() {
    refreshTokenService = new RefreshTokenService(clock, tokenProvider, repository, properties);
  }

  @Nested
  class CreateTokenTests {

    @Test
    void should_CreateAndSaveToken() {
      // Given
      var userId = UUID.randomUUID();
      var userDto = new UserDto(userId, "testuser", true);
      var rawToken = "new-refresh-token";
      var now = OffsetDateTime.now(clock);

      when(tokenProvider.get()).thenReturn(rawToken);
      when(properties.validitySeconds()).thenReturn(VALIDITY_SECONDS);
      when(repository.save(any(RefreshTokenEntity.class))).thenReturn(new RefreshTokenEntity());

      // When
      var result = refreshTokenService.createToken(userDto);

      // Then
      assertThat(result)
          .as("Raw token should be returned to caller, not the hash")
          .isEqualTo(rawToken);

      var captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
      verify(repository).save(captor.capture());

      var savedEntity = captor.getValue();
      assertThat(savedEntity)
          .as("Saved entity should have correct user ID and expiration")
          .returns(userId, RefreshTokenEntity::getUserId)
          .returns(now, RefreshTokenEntity::getCreatedAt)
          .returns(now.plusSeconds(VALIDITY_SECONDS), RefreshTokenEntity::getExpiresAt);

      assertThat(savedEntity.getTokenHash())
          .as("Stored token hash must differ from raw token")
          .isNotEqualTo(rawToken)
          .as("SHA-256 hex digest must be 64 characters")
          .hasSize(64);
    }
  }

  @Nested
  class RotateTests {

    @Test
    void should_RotateToken_When_Valid() {
      // Given
      var rawToken = "old-token";
      var tokenEntity = new RefreshTokenEntity();
      var now = OffsetDateTime.now(clock);
      when(repository.deleteAndReturnValidToken(any(String.class), eq(now)))
          .thenReturn(Optional.of(tokenEntity));

      // When
      var result = refreshTokenService.rotate(rawToken);

      // Then
      assertThat(result)
          .as("Rotate result should be the valid token entity")
          .isSameAs(tokenEntity);

      var captor = ArgumentCaptor.forClass(String.class);
      verify(repository).deleteAndReturnValidToken(captor.capture(), eq(now));
      assertThat(captor.getValue())
          .as("Repository must be called with the hash, not the raw token")
          .isNotEqualTo(rawToken)
          .as("SHA-256 hex digest must be 64 characters")
          .hasSize(64);
    }

    @Test
    void should_ThrowException_When_TokenInvalid() {
      // Given
      var invalidToken = "invalid-token";
      var now = OffsetDateTime.now(clock);
      when(repository.deleteAndReturnValidToken(any(String.class), eq(now)))
          .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> refreshTokenService.rotate(invalidToken))
          .as("Should throw BadCredentialsException when an invalid refresh token is rotated")
          .isInstanceOf(BadCredentialsException.class)
          .hasMessage("Refresh token is invalid");

      var captor = ArgumentCaptor.forClass(String.class);
      verify(repository).deleteAndReturnValidToken(captor.capture(), eq(now));
      assertThat(captor.getValue())
          .as("Repository must be called with the hash, not the raw token")
          .isNotEqualTo(invalidToken)
          .hasSize(64);
    }
  }

  @Nested
  class RevokeTests {

    @Test
    void should_RevokeAllTokensForUser() {
      // Given
      var userId = UUID.randomUUID();

      // When
      refreshTokenService.revokeAll(userId);

      // Then
      verify(repository).deleteAllByUserId(userId);
    }
  }

  @Nested
  class CleanupTests {

    @Test
    void should_DeleteExpiredTokens() {
      // When
      refreshTokenService.deleteExpired();

      // Then
      verify(repository).deleteAllExpired(OffsetDateTime.now(clock));
    }
  }
}
