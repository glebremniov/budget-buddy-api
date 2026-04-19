package com.budget.buddy.budget_buddy_api.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing users.
 * Users are provisioned automatically via JIT provisioning on first OIDC login.
 */
@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository repository;

  /**
   * Finds or creates a local user for the given OIDC subject.
   * On first login, a new user is provisioned automatically (JIT provisioning).
   * Handles concurrent first-login race conditions by catching constraint violations
   * and retrying the lookup.
   *
   * @param oidcSubject the OIDC subject identifier (JWT sub claim)
   * @param jwt the JWT token (reserved for future use)
   * @return the local user's UUID
   */
  @Transactional
  public UUID findOrCreateByOidcSubject(String oidcSubject, Jwt jwt) {
    return repository.findByOidcSubject(oidcSubject)
        .map(UserEntity::getId)
        .orElseGet(() -> {
          log.info("Provisioning new user for OIDC subject: {}", oidcSubject);
          try {
            var user = UserEntity.builder()
                .oidcSubject(oidcSubject)
                .build();
            return repository.save(user).getId();
          } catch (DataIntegrityViolationException e) {
            return repository.findByOidcSubject(oidcSubject)
                .map(UserEntity::getId)
                .orElseThrow(() -> e);
          }
        });
  }
}
