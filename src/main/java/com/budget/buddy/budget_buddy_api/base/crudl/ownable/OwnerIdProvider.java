package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import org.springframework.security.core.AuthenticationException;

import java.util.function.Supplier;

/**
 * Resolves the ID of the currently authenticated owner.
 *
 * <p>The production binding reads the user ID from the
 * {@link org.springframework.security.core.context.SecurityContextHolder}
 * — see {@code OidcOwnerIdProvider}.
 *
 * @param <ID> the owner identifier type
 */
@FunctionalInterface
public interface OwnerIdProvider<ID> extends Supplier<ID> {

  /**
   * Returns the ID of the currently authenticated owner.
   *
   * @return the current owner's ID; never {@code null}
   * @throws AuthenticationException if no authenticated principal is present
   */
  @Override
  ID get() throws AuthenticationException;

}
