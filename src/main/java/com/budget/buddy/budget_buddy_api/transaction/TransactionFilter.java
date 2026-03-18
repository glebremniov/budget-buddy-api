package com.budget.buddy.budget_buddy_api.transaction;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionFilter(
    UUID ownerId,
    UUID categoryId,
    LocalDate start,
    LocalDate end
) {

  public static TransactionFilter of(
      UUID categoryId,
      LocalDate start,
      LocalDate end
  ) {
    return new TransactionFilter(null, categoryId, start, end);
  }

  public TransactionFilter withOwnerId(UUID ownerId) {
    return new TransactionFilter(ownerId, categoryId, start, end);
  }
}
