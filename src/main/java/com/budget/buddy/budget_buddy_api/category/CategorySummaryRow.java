package com.budget.buddy.budget_buddy_api.category;

import java.util.UUID;

record CategorySummaryRow(
    UUID categoryId,
    String categoryName,
    Long monthlyBudget,
    long spent,
    int transactionCount,
    int excludedTransactionCount
) {}
