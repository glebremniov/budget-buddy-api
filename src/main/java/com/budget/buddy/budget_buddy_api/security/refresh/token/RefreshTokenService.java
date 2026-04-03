package com.budget.buddy.budget_buddy_api.security.refresh.token;

import com.budget.buddy.budget_buddy_api.security.TokenService;
import com.budget.buddy.budget_buddy_api.user.UserDto;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
   * Create and persist a new refresh token for the given user.
   * The raw token is returned to the caller; only its SHA-256 hash is stored.
   *
   * @param user user DTO
   * @return raw opaque refresh token string
   */
  @Transactional
  @Override
  public String createToken(UserDto user) {
    var rawToken = tokenProvider.get();
    var now = OffsetDateTime.now(clock);

    var entity = new RefreshTokenEntity();
    entity.setTokenHash(hash(rawToken));
    entity.setUserId(user.id());
    entity.setCreatedAt(now);
    entity.setExpiresAt(now.plusSeconds(properties.validitySeconds()));

    repository.save(entity);
    return rawToken;
  }

  /**
   * Validate refresh token and rotate. Atomically deletes old token and returns entity for issuing new tokens.
   *
   * @param refreshToken raw opaque refresh token presented by the client
   * @return validated RefreshTokenEntity
   * @throws BadCredentialsException if token is invalid or expired
   */
  @Transactional
  public RefreshTokenEntity rotate(String refreshToken) {
    var now = OffsetDateTime.now(clock);

    return repository.deleteAndReturnValidToken(hash(refreshToken), now)
        .orElseThrow(() -> new BadCredentialsException("Refresh token is invalid"));
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

  private static String hash(String token) {
    try {
      return HexFormat.of().formatHex(
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

}
