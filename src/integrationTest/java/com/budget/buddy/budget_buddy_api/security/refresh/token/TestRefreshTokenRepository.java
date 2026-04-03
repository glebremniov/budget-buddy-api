package com.budget.buddy.budget_buddy_api.security.refresh.token;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Test-only repository for refresh token assertions.
 * Allows querying tokens without consuming them.
 */
@Repository
public interface TestRefreshTokenRepository extends CrudRepository<RefreshTokenEntity, UUID> {

  /**
   * Find a valid (non-expired) refresh token by its SHA-256 hash.
   * This method is for testing only, as it doesn't consume the token.
   */
  @Query("SELECT * FROM refresh_tokens WHERE token_hash = :tokenHash AND expires_at > :now")
  Optional<RefreshTokenEntity> findValidToken(String tokenHash, OffsetDateTime now);
}
