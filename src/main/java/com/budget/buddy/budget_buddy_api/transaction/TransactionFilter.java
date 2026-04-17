package com.budget.buddy.budget_buddy_api.transaction;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionFilter(
    UUID ownerId,
    UUID categoryId,
    LocalDate start,
    LocalDate end,
    TransactionType type
) {

  public static TransactionFilter of(
      UUID categoryId,
      LocalDate start,
      LocalDate end,
      TransactionType type
  ) {
    return new TransactionFilter(null, categoryId, start, end, type);
  }

  public TransactionFilter withOwnerId(UUID ownerId) {
    return new TransactionFilter(ownerId, categoryId, start, end, type);
  }
}
