package com.budget.buddy.budget_buddy_api.transaction;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionFilter(
    UUID ownerId,
    UUID categoryId,
    LocalDate start,
    LocalDate end,
    TransactionType type,
    String query,
    Long amountMin,
    Long amountMax
) {

  public static TransactionFilter of(
      UUID categoryId,
      LocalDate start,
      LocalDate end,
      TransactionType type,
      String query,
      Long amountMin,
      Long amountMax
  ) {
    return new TransactionFilter(null, categoryId, start, end, type, query, amountMin, amountMax);
  }

  public TransactionFilter withOwnerId(UUID ownerId) {
    return new TransactionFilter(ownerId, categoryId, start, end, type, query, amountMin, amountMax);
  }
}
