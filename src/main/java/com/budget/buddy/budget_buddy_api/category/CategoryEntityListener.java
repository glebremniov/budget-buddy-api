package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.AuditableEntityListener;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class CategoryEntityListener extends AuditableEntityListener<CategoryEntity, UUID> {

  public CategoryEntityListener(Supplier<UUID> idGenerator, Clock clock) {
    super(idGenerator, clock);
  }
}
