package com.budget.buddy.budget_buddy_api.user;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Repository for User entity operations using Spring Data JDBC.
 */
public interface UserRepository extends Repository<UserEntity, UUID> {

  String UPSERT_QUERY = """
      WITH ins AS (
        INSERT INTO users (id, oidc_subject, oidc_issuer, version, created_at, updated_at)
        VALUES (:id, :oidcSubject, :oidcIssuer, 0, NOW(), NOW())
        ON CONFLICT (oidc_subject, oidc_issuer) DO NOTHING
        RETURNING id
      )
      SELECT id FROM ins
      UNION ALL
      SELECT id FROM users
      WHERE oidc_subject = :oidcSubject AND oidc_issuer = :oidcIssuer
      LIMIT 1
      """;

  /**
   * Atomically inserts a new user if no row with the given OIDC subject exists.
   * Uses {@code ON CONFLICT DO NOTHING ... RETURNING id} so concurrent calls are safe.
   *
   * @return {@link UUID} id of the user
   */
  @Query(UPSERT_QUERY)
  UUID upsert(
      @Param("id") UUID id,
      @Param("oidcSubject") String oidcSubject,
      @Param("oidcIssuer") String oidcIssuer
  );

}
