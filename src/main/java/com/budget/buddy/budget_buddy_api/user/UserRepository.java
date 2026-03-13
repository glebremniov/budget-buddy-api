package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entity operations using Spring Data JDBC.
 */
@Repository
public interface UserRepository extends BaseRepository<UserEntity, UUID> {

  /**
   * Find user by username
   *
   * @param username user name
   * @return Optional containing user if found
   */
  Optional<UserEntity> findByUsername(String username);

  /**
   * Check if user exists by username
   *
   * @param username user name
   * @return true if user exists
   */
  boolean existsByUsername(String username);

}
