package com.budget.buddy.budget_buddy_api.security.oidc;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Provides the local user UUID set by {@link OidcUserProvisioningFilter}.
 * The filter runs after JWT authentication and maps the OIDC subject to a local user,
 * storing the resulting UUID as a request attribute.
 */
@Component
public class OidcOwnerIdProvider implements OwnerIdProvider<UUID> {

  @Override
  public UUID get() throws AuthenticationException {
    var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      throw new InvalidBearerTokenException("No request context available.");
    }

    HttpServletRequest request = attrs.getRequest();
    var userId = (UUID) request.getAttribute(OidcUserProvisioningFilter.USER_ID_ATTRIBUTE);
    if (userId == null) {
      throw new InvalidBearerTokenException("Current user is not authenticated.");
    }

    return userId;
  }
}
