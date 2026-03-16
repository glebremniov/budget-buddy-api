package com.budget.buddy.budget_buddy_api.base.crudl;

import org.springframework.data.domain.Persistable;

@SuppressWarnings("java:S119")
public interface BaseEntity<ID> extends Persistable<ID> {

  void setId(ID id);

  @Override
  default boolean isNew() {
    return getId() == null;
  }
}
