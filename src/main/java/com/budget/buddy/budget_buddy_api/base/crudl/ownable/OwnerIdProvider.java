package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import java.util.function.Supplier;
import org.springframework.security.core.AuthenticationException;

/**
 * Resolves the ID of the currently authenticated owner.
 *
 * <p>Implementations are expected to read the identity from the active security context.
 * The production binding lives in {@code ApplicationConfig} and delegates to
 * {@link com.budget.buddy.budget_buddy_api.security.auth.AuthUtils}.
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
