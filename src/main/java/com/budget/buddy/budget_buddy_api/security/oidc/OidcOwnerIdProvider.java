package com.budget.buddy.budget_buddy_api.security.oidc;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the local user UUID from the {@link LocalUserAuthentication} placed
 * on the {@link SecurityContextHolder} by {@link OidcUserProvisioningFilter}.
 *
 * <p>Reading from the security context (instead of a request attribute) keeps
 * this provider usable from any thread that propagates the security context —
 * e.g. {@code @Async} or scheduled tasks.
 */
@Component
public class OidcOwnerIdProvider implements OwnerIdProvider<UUID> {

  @Override
  public UUID get() throws AuthenticationException {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof LocalUserAuthentication local) {
      return local.getLocalUserId();
    }
    throw new InvalidBearerTokenException("Current user is not authenticated.");
  }
}
