package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntity;

/**
 * Interface for entities that belong to a user.
 *
 * @param <ID> the user identifier type
 */
@SuppressWarnings("java:S119")
public interface OwnableEntity<ID> extends BaseEntity<ID> {

  /**
   * Retrieves the ID of the user who owns this entity.
   *
   * @return the owner ID
   */
  ID getOwnerId();

  /**
   * Sets the ID of the user who owns this entity.
   *
   * @param id the owner ID
   */
  void setOwnerId(ID id);

}
