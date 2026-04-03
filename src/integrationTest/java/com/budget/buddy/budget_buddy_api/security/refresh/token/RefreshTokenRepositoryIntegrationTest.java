package com.budget.buddy.budget_buddy_api.security.refresh.token;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseIntegrationTest;
import com.budget.buddy.budget_buddy_api.user.UserEntity;
import com.budget.buddy.budget_buddy_api.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
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
    var validHash = sha256(UUID.randomUUID().toString());
    var expiredHash = sha256(UUID.randomUUID().toString());

    var validEntity = RefreshTokenEntity.builder()
        .tokenHash(validHash)
        .userId(userId)
        .createdAt(now)
        .expiresAt(now.plusDays(1))
        .build();
    refreshTokenRepository.save(validEntity);

    var expiredEntity = RefreshTokenEntity.builder()
        .tokenHash(expiredHash)
        .userId(userId)
        .createdAt(now.minusDays(2))
        .expiresAt(now.minusDays(1))
        .build();
    refreshTokenRepository.save(expiredEntity);

    // When
    var foundValid = refreshTokenRepository.findValidToken(validHash, now);
    var foundExpired = refreshTokenRepository.findValidToken(expiredHash, now);

    // Then
    assertThat(foundValid).isPresent();
    assertThat(foundValid.get().getTokenHash()).isEqualTo(validHash);
    assertThat(foundExpired).isEmpty();
  }

  @Test
  @DisplayName("should delete all tokens by user ID")
  void shouldDeleteAllByUserId() {
    // Given
    var t1Hash = sha256(UUID.randomUUID().toString());
    var t1 = RefreshTokenEntity.builder()
        .tokenHash(t1Hash)
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
    var t2Hash = sha256(UUID.randomUUID().toString());
    var t2 = RefreshTokenEntity.builder()
        .tokenHash(t2Hash)
        .userId(otherUserId)
        .createdAt(OffsetDateTime.now())
        .expiresAt(OffsetDateTime.now().plusDays(1))
        .build();
    refreshTokenRepository.save(t2);

    // When
    refreshTokenRepository.deleteAllByUserId(userId);

    // Then
    assertThat(refreshTokenRepository.findAll()).hasSize(1);
    assertThat(refreshTokenRepository.findValidToken(t2Hash, OffsetDateTime.now())).isPresent();
    assertThat(refreshTokenRepository.findValidToken(t1Hash, OffsetDateTime.now())).isEmpty();
  }

  @Test
  @DisplayName("should delete all expired tokens")
  void shouldDeleteAllExpired() {
    // Given
    var now = OffsetDateTime.now();
    var e1Hash = sha256(UUID.randomUUID().toString());
    var e1 = RefreshTokenEntity.builder()
        .tokenHash(e1Hash)
        .userId(userId)
        .createdAt(now.minusDays(5))
        .expiresAt(now.minusDays(1))
        .build();
    refreshTokenRepository.save(e1);

    var v1Hash = sha256(UUID.randomUUID().toString());
    var v1 = RefreshTokenEntity.builder()
        .tokenHash(v1Hash)
        .userId(userId)
        .createdAt(now)
        .expiresAt(now.plusDays(1))
        .build();
    refreshTokenRepository.save(v1);

    // When
    refreshTokenRepository.deleteAllExpired(now);

    // Then
    assertThat(refreshTokenRepository.findAll()).hasSize(1);
    assertThat(refreshTokenRepository.findValidToken(v1Hash, now)).isPresent();
    assertThat(refreshTokenRepository.findValidToken(e1Hash, now)).isEmpty();
  }

  private static String sha256(String input) {
    try {
      return HexFormat.of().formatHex(
          MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
