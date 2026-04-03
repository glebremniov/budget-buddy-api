package com.budget.buddy.budget_buddy_api.security.refresh.token;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for refresh token operations.
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, UUID> {

  /**
   * Find a valid (non-expired) refresh token by its SHA-256 hash
   */
  @Query("SELECT * FROM refresh_tokens WHERE token_hash = :tokenHash AND expires_at > :now")
  Optional<RefreshTokenEntity> findValidToken(String tokenHash, OffsetDateTime now);

  /**
   * Delete all refresh tokens for a user (logout all sessions)
   */
  @Modifying
  void deleteAllByUserId(UUID userId);

  /**
   * Delete all expired tokens (for cleanup job)
   */
  @Modifying
  @Query("DELETE FROM refresh_tokens WHERE expires_at < :now")
  void deleteAllExpired(OffsetDateTime now);

  /**
   * Atomically consume a valid refresh token by its SHA-256 hash.
   * Returns the deleted entity if found and not expired, empty otherwise.
   * Eliminates TOCTOU race condition in token rotation.
   */
  @Query("DELETE FROM refresh_tokens WHERE token_hash = :tokenHash AND expires_at > :now RETURNING *")
  Optional<RefreshTokenEntity> deleteAndReturnValidToken(String tokenHash, OffsetDateTime now);
}
