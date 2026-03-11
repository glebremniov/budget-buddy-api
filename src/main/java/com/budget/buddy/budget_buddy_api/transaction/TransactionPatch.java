package com.budget.buddy.budget_buddy_api.transaction;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionPatch(
    UUID categoryId,
    Integer amount,
    TransactionType type,
    String currency,
    LocalDate date,
    String description
) {

}
