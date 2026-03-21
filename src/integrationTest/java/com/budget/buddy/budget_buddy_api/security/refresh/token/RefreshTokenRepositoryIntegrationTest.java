package com.budget.buddy.budget_buddy_api.security.refresh.token;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseIntegrationTest;
import com.budget.buddy.budget_buddy_api.user.UserEntity;
import com.budget.buddy.budget_buddy_api.user.UserRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("RefreshTokenRepository Integration Tests")
class RefreshTokenRepositoryIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private UserRepository userRepository;

  private UUID userId;

  @BeforeEach
  void setUp() {
    var user = UserEntity.builder()
        .username("token_user_" + UUID.randomUUID())
        .password("password")
        .enabled(true)
        .build();
    userId = userRepository.save(user).getId();
  }

  @Test
  @DisplayName("should find valid token and ignore expired ones")
  void shouldFindValidToken() {
    // Given
    var now = OffsetDateTime.now();
    var validToken = "valid_" + UUID.randomUUID();
    var expiredToken = "expired_" + UUID.randomUUID();

    var validEntity = RefreshTokenEntity.builder()
        .token(validToken)
        .userId(userId)
        .createdAt(now)
        .expiresAt(now.plusDays(1))
        .build();
    refreshTokenRepository.save(validEntity);

    var expiredEntity = RefreshTokenEntity.builder()
        .token(expiredToken)
        .userId(userId)
        .createdAt(now.minusDays(2))
        .expiresAt(now.minusDays(1))
        .build();
    refreshTokenRepository.save(expiredEntity);

    // When
    var foundValid = refreshTokenRepository.findValidToken(validToken, now);
    var foundExpired = refreshTokenRepository.findValidToken(expiredToken, now);

    // Then
    assertThat(foundValid).isPresent();
    assertThat(foundValid.get().getToken()).isEqualTo(validToken);
    assertThat(foundExpired).isEmpty();
  }

  @Test
  @DisplayName("should delete all tokens by user ID")
  void shouldDeleteAllByUserId() {
    // Given
    var t1 = RefreshTokenEntity.builder()
        .token("t1_" + UUID.randomUUID())
        .userId(userId)
        .createdAt(OffsetDateTime.now())
        .expiresAt(OffsetDateTime.now().plusDays(1))
        .build();
    refreshTokenRepository.save(t1);

    var otherUserId = userRepository.save(UserEntity.builder()
        .username("other_user_" + UUID.randomUUID())
        .password("password")
        .enabled(true)
        .build()).getId();
    var t2 = RefreshTokenEntity.builder()
        .token("t2_" + UUID.randomUUID())
        .userId(otherUserId)
        .createdAt(OffsetDateTime.now())
        .expiresAt(OffsetDateTime.now().plusDays(1))
        .build();
    refreshTokenRepository.save(t2);

    // When
    refreshTokenRepository.deleteAllByUserId(userId);

    // Then
    assertThat(refreshTokenRepository.findAll()).hasSize(1);
    assertThat(refreshTokenRepository.findValidToken(t2.getToken(), OffsetDateTime.now())).isPresent();
    assertThat(refreshTokenRepository.findValidToken(t1.getToken(), OffsetDateTime.now())).isEmpty();
  }

  @Test
  @DisplayName("should delete all expired tokens")
  void shouldDeleteAllExpired() {
    // Given
    var now = OffsetDateTime.now();
    var e1 = RefreshTokenEntity.builder()
        .token("e1_" + UUID.randomUUID())
        .userId(userId)
        .createdAt(now.minusDays(5))
        .expiresAt(now.minusDays(1))
        .build();
    refreshTokenRepository.save(e1);

    var v1 = RefreshTokenEntity.builder()
        .token("v1_" + UUID.randomUUID())
        .userId(userId)
        .createdAt(now)
        .expiresAt(now.plusDays(1))
        .build();
    refreshTokenRepository.save(v1);

    // When
    refreshTokenRepository.deleteAllExpired(now);

    // Then
    assertThat(refreshTokenRepository.findAll()).hasSize(1);
    assertThat(refreshTokenRepository.findValidToken(v1.getToken(), now)).isPresent();
    assertThat(refreshTokenRepository.findValidToken(e1.getToken(), now)).isEmpty();
  }
}
