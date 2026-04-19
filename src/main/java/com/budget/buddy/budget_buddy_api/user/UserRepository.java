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
      INSERT INTO users 
          (id, oidc_subject, version, created_at, updated_at)
      VALUES 
          (:id, :oidcSubject, 0, NOW(), NOW())
      ON CONFLICT (oidc_subject) DO UPDATE SET oidc_subject = EXCLUDED.oidc_subject
      RETURNING id;
      """;

  /**
   * Atomically inserts a new user if no row with the given OIDC subject exists.
   * Uses {@code ON CONFLICT DO UPDATE SET ... RETURNING id} so concurrent calls are safe.
   *
   * @return {@link UUID} id of the user
   */
  @Query(UPSERT_QUERY)
  UUID upsert(@Param("id") UUID id, @Param("oidcSubject") String oidcSubject);

}
