package com.budget.buddy.budget_buddy_api.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Service for managing users.
 * Users are provisioned automatically via JIT provisioning on first OIDC login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository repository;
  private final Supplier<UUID> idGenerator;

  /**
   * Finds or creates a local user for the given OIDC subject.
   * On first login, a new user is provisioned automatically (JIT provisioning).
   * first-login requests are safe without try-catch or retries.
   *
   * @param oidcSubject the OIDC subject identifier (JWT sub claim)
   * @return the local user's UUID
   */
  @Transactional
  public UUID findOrCreateByOidcSubject(String oidcSubject, String oidcIssuer) {
    log.info("Provisioning or retrieving local ID for OIDC subject: {}, issuer: {}", oidcSubject, oidcIssuer);
    return repository.upsert(idGenerator.get(), oidcSubject, oidcIssuer);
  }
}
