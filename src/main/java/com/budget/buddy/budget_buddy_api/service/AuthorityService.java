package com.budget.buddy.budget_buddy_api.service;

import com.budget.buddy.budget_buddy_api.entity.AuthorityEntity;
import com.budget.buddy.budget_buddy_api.repository.AuthorityRepository;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user authorities/roles.
 */
@Service
public class AuthorityService {

  private final AuthorityRepository authorityRepository;

  public AuthorityService(AuthorityRepository authorityRepository) {
    this.authorityRepository = authorityRepository;
  }

  private static Predicate<AuthorityEntity> hasAuthority(String authority) {
    return auth -> auth.getAuthority().equals(authority);
  }

  /**
   * Get all authorities for a user.
   *
   * @param username the username
   * @return list of authority strings
   */
  public List<String> getAuthoritiesForUser(String username) {
    return authorityRepository.findAllByUsername(username)
        .stream()
        .map(AuthorityEntity::getAuthority)
        .collect(Collectors.toList());
  }

  /**
   * Add an authority to a user.
   *
   * @param username the username
   * @param authority the authority to add
   */
  @Transactional
  public void addRoleToUser(String username, String authority) {
    var authorityEntity = new AuthorityEntity(username, authority);
    authorityRepository.save(authorityEntity);
  }

  /**
   * Remove am authority from a user.
   *
   * @param username the username
   * @param authority the authority to remove
   */
  @Transactional
  public void removeAuthorityFromUser(String username, String authority) {
    var authorities = authorityRepository.findAllByUsername(username);
    var toDelete = authorities.stream()
        .filter(auth -> auth.getAuthority().equals(authority))
        .toList();

    authorityRepository.deleteAll(toDelete);
  }

  /**
   * Check if a user has a specific authority.
   *
   * @param username the username
   * @param authority the authority to check
   * @return true if the user has the authority, false otherwise
   */
  public boolean userHasAuthority(String username, String authority) {
    return authorityRepository.existsByUsernameAndAuthority(username, authority);
  }
}
