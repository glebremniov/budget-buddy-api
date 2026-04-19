package com.budget.buddy.budget_buddy_api.security.oidc;

import com.budget.buddy.budget_buddy_api.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Ensures a local {@code UserEntity} exists for every authenticated OIDC user.
 * On the first request from a new user, a record is created automatically (JIT provisioning).
 * The resolved local user ID is stored as a request attribute for downstream use by
 * {@link com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider}.
 */
@Slf4j
@RequiredArgsConstructor
public class OidcUserProvisioningFilter extends OncePerRequestFilter {

  /**
   * Request attribute key where the local user UUID is stored after provisioning.
   */
  public static final String USER_ID_ATTRIBUTE = OidcUserProvisioningFilter.class.getName() + ".USER_ID";

  private final UserService userService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      var oidcSubject = jwt.getSubject();

      if (oidcSubject == null) {
        throw new InsufficientAuthenticationException("JWT subject is missing");
      }

      UUID localUserId = userService.findOrCreateByOidcSubject(oidcSubject);
      request.setAttribute(USER_ID_ATTRIBUTE, localUserId);
    }

    filterChain.doFilter(request, response);
  }
}
