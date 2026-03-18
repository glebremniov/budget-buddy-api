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
public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, String> {

  /**
   * Find a valid (non-expired) refresh token in a single query
   */
  @Query("SELECT * FROM refresh_tokens WHERE token = :token AND expires_at > :now")
  Optional<RefreshTokenEntity> findValidToken(String token, OffsetDateTime now);

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

}
