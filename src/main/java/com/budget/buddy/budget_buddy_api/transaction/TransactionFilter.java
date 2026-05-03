package com.budget.buddy.budget_buddy_api.transaction;

import lombok.Builder;
import lombok.With;

import java.time.LocalDate;
import java.util.UUID;

@With
@Builder
public record TransactionFilter(
    UUID ownerId,
    UUID categoryId,
    LocalDate start,
    LocalDate end,
    TransactionType type,
    String query,
    Long amountMin,
    Long amountMax
) {}
