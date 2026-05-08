package com.budget.buddy.budget_buddy_api.transaction;

import java.time.YearMonth;

/**
 * One calendar-month bucket emitted by the trend query: the month it represents
 * paired with the aggregated {@link TransactionSummaryRow} for that month.
 */
public record TransactionTrendBucket(YearMonth month, TransactionSummaryRow row) {
}
