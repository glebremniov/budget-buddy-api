package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.MonthlySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds {@link MonthlySummary} responses from raw transaction aggregates. Owns parsing of
 * the {@code YYYY-MM} string inputs, range validation, the empty-month zero-fill on trend
 * queries, and the final {@link TransactionSummaryRow} → {@link MonthlySummary} mapping.
 * The repository stays focused on SQL; the controller stays focused on HTTP.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionSummaryService {

  /** Caps the per-request bucket count so a single query can't fan out to arbitrary depth. */
  static final int MAX_RANGE_MONTHS = 24;

  private static final TransactionSummaryRow EMPTY_ROW = new TransactionSummaryRow(0L, 0L, 0, 0, 0);

  private final TransactionSummaryRepository repository;
  private final OwnerIdProvider<UUID> ownerIdProvider;

  /**
   * Compute the {@link MonthlySummary} for a single calendar month.
   *
   * @param month    calendar month formatted {@code YYYY-MM}
   * @param currency ISO 4217 currency code; only matching transactions contribute to totals
   * @throws IllegalArgumentException if {@code month} is not a valid {@code YYYY-MM} value
   */
  public MonthlySummary getSummary(String month, String currency) {
    var yearMonth = parseMonth(month, "month");
    var row = repository.getSummary(
        ownerIdProvider.get(), yearMonth.atDay(1), yearMonth.atEndOfMonth(), currency);
    return toMonthlySummary(yearMonth, currency, row);
  }

  /**
   * Compute one {@link MonthlySummary} per calendar month across the inclusive
   * {@code [fromMonth, toMonth]} range, oldest first. Months with no matching transactions
   * are zero-filled so the returned list always has {@code (toMonth - fromMonth) + 1} elements.
   *
   * @param fromMonth first calendar month, inclusive ({@code YYYY-MM})
   * @param toMonth   last calendar month, inclusive ({@code YYYY-MM})
   * @param currency  ISO 4217 currency code
   * @throws IllegalArgumentException if either bound is malformed, {@code fromMonth} is after
   *                                  {@code toMonth}, or the span exceeds {@link #MAX_RANGE_MONTHS}
   */
  public List<MonthlySummary> getTrend(String fromMonth, String toMonth, String currency) {
    var from = parseMonth(fromMonth, "from");
    var to = parseMonth(toMonth, "to");
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("'from' must be on or before 'to'");
    }
    long span = ChronoUnit.MONTHS.between(from, to) + 1;
    if (span > MAX_RANGE_MONTHS) {
      throw new IllegalArgumentException(
          "Range too wide: max " + MAX_RANGE_MONTHS + " months, requested " + span);
    }

    var byMonth = repository.getTrend(
        ownerIdProvider.get(), from.atDay(1), to.atEndOfMonth(), currency);

    List<MonthlySummary> out = new ArrayList<>((int) span);
    for (var ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
      out.add(toMonthlySummary(ym, currency, byMonth.getOrDefault(ym, EMPTY_ROW)));
    }
    return out;
  }

  private static MonthlySummary toMonthlySummary(
      YearMonth ym, String currency, TransactionSummaryRow row) {
    return new MonthlySummary(
        ym.toString(),
        currency,
        row.income(),
        row.expense(),
        row.income() - row.expense(),
        row.incomeCount(),
        row.expenseCount(),
        row.excludedCount()
    );
  }

  private static YearMonth parseMonth(String value, String paramName) {
    try {
      return YearMonth.parse(value);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid '" + paramName + "' format: '" + value + "', expected YYYY-MM", e);
    }
  }

}
