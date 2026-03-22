package com.budget.buddy.budget_buddy_api.security.refresh.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
      var generatedToken = "new-refresh-token";
      var now = OffsetDateTime.now(clock);

      when(tokenProvider.get()).thenReturn(generatedToken);
      when(properties.validitySeconds()).thenReturn(VALIDITY_SECONDS);

      var entity = new RefreshTokenEntity();
      entity.setToken(generatedToken);
      when(repository.save(any(RefreshTokenEntity.class))).thenReturn(entity);

      // When
      var result = refreshTokenService.createToken(userDto);

      // Then
      assertThat(result)
          .as("Resulting token should match the generated token")
          .isEqualTo(generatedToken);

      var captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
      verify(repository).save(captor.capture());

      var savedEntity = captor.getValue();
      assertThat(savedEntity)
          .as("Saved entity should have correct token, user ID, and expiration")
          .returns(generatedToken, RefreshTokenEntity::getToken)
          .returns(userId, RefreshTokenEntity::getUserId)
          .returns(now, RefreshTokenEntity::getCreatedAt)
          .returns(now.plusSeconds(VALIDITY_SECONDS), RefreshTokenEntity::getExpiresAt);
    }
  }

  @Nested
  class RotateTests {

    @Test
    void should_RotateToken_When_Valid() {
      // Given
      var oldToken = "old-token";
      var tokenEntity = new RefreshTokenEntity();
      tokenEntity.setToken(oldToken);

      when(repository.findValidToken(oldToken, OffsetDateTime.now(clock)))
          .thenReturn(Optional.of(tokenEntity));

      // When
      var result = refreshTokenService.rotate(oldToken);

      // Then
      assertThat(result)
          .as("Rotate result should be the valid token entity")
          .isSameAs(tokenEntity);

      verify(repository).delete(tokenEntity);
    }

    @Test
    void should_ThrowException_When_TokenInvalid() {
      // Given
      var invalidToken = "invalid-token";
      when(repository.findValidToken(invalidToken, OffsetDateTime.now(clock)))
          .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> refreshTokenService.rotate(invalidToken))
          .as("Should throw BadCredentialsException when an invalid refresh token is rotated")
          .isInstanceOf(BadCredentialsException.class)
          .hasMessage("Refresh token is invalid");
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
