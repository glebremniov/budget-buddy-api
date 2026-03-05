package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseEntity;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.security.auth.AuthService;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for user-related operations. Provides methods to retrieve information about the currently authenticated user.
 */
@Service
public class UserService {

  private final AuthService authService;
  private final UserRepository userRepository;

  public UserService(AuthService authService, UserRepository userRepository) {
    this.authService = authService;
    this.userRepository = userRepository;
  }

  /**
   * Get the username of the currently authenticated user
   *
   * @return Optional containing username if authenticated, empty otherwise
   */
  public Optional<String> getCurrentUserName() {
    return authService.getCurrentUserName();
  }

  /**
   * Get the user ID of the currently authenticated user
   *
   * @return user ID
   * @throws AuthenticationCredentialsNotFoundException if no authenticated user is found
   */
  @NonNull
  public UUID getCurrentUserIdOrThrow() {
    var currentUserName = getCurrentUserName()
        .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("No authenticated user found"));

    return userRepository.findByUsername(currentUserName)
        .map(BaseEntity::getId)
        .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found in database"));
  }

}
