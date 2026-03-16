package com.budget.buddy.budget_buddy_api.security.refresh.token;

import com.budget.buddy.budget_buddy_api.security.TokenService;
import com.budget.buddy.budget_buddy_api.user.UserDto;
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
public class RefreshTokenService implements TokenService<String> {

  private final Clock clock;
  private final RefreshTokenProvider tokenProvider;
  private final RefreshTokenRepository repository;
  private final RefreshTokenProperties properties;

  /**
   * Create and persist a new refresh token for the given user
   *
   * @param user user ID
   * @return opaque refresh token string
   */
  @Transactional
  @Override
  public String createToken(UserDto user) {
    var now = OffsetDateTime.now(clock);
    var token = RefreshTokenEntity.builder()
        .token(tokenProvider.get())
        .userId(user.id())
        .createdAt(now)
        .expiresAt(now.plusSeconds(properties.validitySeconds()))
        .build();
    return repository.save(token)
        .getToken();
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

    var tokenEntity = repository.findByToken(refreshToken)
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
