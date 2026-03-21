package com.budget.buddy.budget_buddy_api.base.crudl.base;

/**
 * Functional interface for validating entities before they are saved.
 *
 * @param <E> the entity type
 */
@FunctionalInterface
public interface BaseEntityValidator<E extends BaseEntity<?>> {

  /**
   * Validates the provided entity.
   *
   * @param entity the entity to validate
   * @throws RuntimeException if validation fails
   */
  void validate(E entity);

}
