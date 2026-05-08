package com.budget.buddy.budget_buddy_api.transaction;

/**
 * Raw aggregate row returned by transaction summary queries: amounts in minor units,
 * counts as exact integers. {@code excludedCount} reports transactions in other
 * currencies that were filtered out of the income / expense totals.
 */
public record TransactionSummaryRow(
    long income,
    long expense,
    int incomeCount,
    int expenseCount,
    int excludedCount
) {}