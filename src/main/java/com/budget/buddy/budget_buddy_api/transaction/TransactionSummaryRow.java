package com.budget.buddy.budget_buddy_api.transaction;

record TransactionSummaryRow(
    long income,
    long expense,
    int incomeCount,
    int expenseCount,
    int excludedCount
) {}
