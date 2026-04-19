package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations using Spring Data JDBC.
 */
@Repository
public interface UserRepository extends BaseEntityRepository<UserEntity, UUID> {

  /**
   * Find user by OIDC subject (JWT sub claim)
   *
   * @param oidcSubject the OIDC subject identifier
   * @return Optional containing user if found
   */
  Optional<UserEntity> findByOidcSubject(String oidcSubject);

}
