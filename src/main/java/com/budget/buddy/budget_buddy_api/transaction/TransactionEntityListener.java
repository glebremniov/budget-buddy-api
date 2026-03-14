package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.AuditableEntityListener;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityListener extends AuditableEntityListener<TransactionEntity, UUID> {

  public TransactionEntityListener(Supplier<UUID> idGenerator, Clock clock) {
    super(idGenerator, clock);
  }
}
