package com.budget.buddy.budget_buddy_api.base.crudl.base;

import org.springframework.data.domain.Persistable;

/**
 * Base interface for all entities in the application.
 * Extends {@link Persistable} to provide standard persistence status.
 *
 * @param <ID> the identifier type
 */
@SuppressWarnings("java:S119")
public interface BaseEntity<ID> extends Persistable<ID> {

  /**
   * Sets the unique identifier for the entity.
   *
   * @param id the unique identifier
   */
  void setId(ID id);

  @Override
  default boolean isNew() {
    return getId() == null;
  }
}
