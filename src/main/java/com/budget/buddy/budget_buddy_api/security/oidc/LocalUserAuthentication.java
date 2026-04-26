package com.budget.buddy.budget_buddy_api.security.oidc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.UUID;

/**
 * Authentication token that carries the resolved local user UUID alongside the JWT.
 * Produced once per request by {@link OidcUserProvisioningFilter} and read by
 * {@link OidcOwnerIdProvider}, so downstream code can resolve the owner id directly
 * from the {@link org.springframework.security.core.context.SecurityContextHolder}
 * without touching the servlet request.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class LocalUserAuthentication extends JwtAuthenticationToken {

  private final UUID localUserId;

  public LocalUserAuthentication(Jwt jwt, Collection<? extends GrantedAuthority> authorities, UUID localUserId) {
    super(jwt, authorities);
    setAuthenticated(true);
    this.localUserId = localUserId;
  }
}
