package com.budget.buddy.budget_buddy_api.security.refresh.token;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing opaque refresh tokens. Handles creation, validation, rotation and revocation.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final Clock clock;
  private final RefreshTokenRepository repository;
  private final RefreshTokenProperties properties;

  /**
   * Create and persist a new refresh token for the given user
   *
   * @param userId user ID
   * @return opaque refresh token string
   */
  @Transactional
  public String create(UUID userId) {
    var now = OffsetDateTime.now(clock);
    var token = RefreshTokenEntity.builder()
        .userId(userId)
        .createdAt(now)
        .expiresAt(now.plusSeconds(properties.validitySeconds()))
        .build();
    repository.save(token);
    return token.getId();
  }

  /**
   * Validate refresh token and rotate. Deletes old token and returns entity for issuing new tokens.
   *
   * @param refreshToken opaque refresh token
   * @return validated RefreshTokenEntity
   * @throws BadCredentialsException if token is invalid or expired
   */
  @Transactional
  public RefreshTokenEntity rotate(String refreshToken) {
    var now = OffsetDateTime.now(clock);

    var tokenEntity = repository.findById(refreshToken)
        .orElseThrow(() -> new BadCredentialsException("Refresh token is invalid"));

    if (now.isAfter(tokenEntity.getExpiresAt())) {
      throw new AccountExpiredException("Refresh token is expired");
    }

    repository.delete(tokenEntity);
    return tokenEntity;
  }

  /**
   * Revoke all refresh tokens for a user (logout all sessions)
   *
   * @param userId user ID
   */
  @Transactional
  public void revokeAll(UUID userId) {
    repository.deleteAllByUserId(userId);
  }

  /**
   * Delete all expired tokens — intended for scheduled cleanup
   */
  @Transactional
  public void deleteExpired() {
    repository.deleteAllExpired(OffsetDateTime.now(clock));
  }

}
