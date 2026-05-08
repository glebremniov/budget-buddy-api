package com.budget.buddy.budget_buddy_api.transaction;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Aggregates over the {@code transactions} table for the authenticated user. Issues raw SQL
 * via {@link JdbcClient} so the database does the per-currency, per-type aggregation in a
 * single round-trip; callers receive value-object rows ready for mapping to API DTOs.
 */
@Repository
public class TransactionSummaryRepository {

  private static final String OWNER_ID = "ownerId";
  private static final String CURRENCY = "currency";
  private static final String RANGE_START = "rangeStart";
  private static final String RANGE_END = "rangeEnd";

  private static final RowMapper<TransactionSummaryRow> TRANSACTION_SUMMARY_ROW_ROW_MAPPER = (rs, _) ->
      new TransactionSummaryRow(
          rs.getLong("income"),
          rs.getLong("expense"),
          rs.getInt("income_count"),
          rs.getInt("expense_count"),
          rs.getInt("excluded_count")
      );

  private static final RowMapper<TransactionTrendBucket> TREND_BUCKET_ROW_MAPPER = (rs, rowNum) ->
      new TransactionTrendBucket(
          YearMonth.from(rs.getDate("month_start").toLocalDate()),
          TRANSACTION_SUMMARY_ROW_ROW_MAPPER.mapRow(rs, rowNum)
      );

  private static final String SUMMARY_SQL = """
      SELECT
          COALESCE(SUM(amount) FILTER (WHERE currency = :currency AND type = 'INCOME'),  0) AS income,
          COALESCE(SUM(amount) FILTER (WHERE currency = :currency AND type = 'EXPENSE'), 0) AS expense,
          COALESCE(COUNT(*)    FILTER (WHERE currency = :currency AND type = 'INCOME'),  0) AS income_count,
          COALESCE(COUNT(*)    FILTER (WHERE currency = :currency AND type = 'EXPENSE'), 0) AS expense_count,
          COALESCE(COUNT(*)    FILTER (WHERE currency <> :currency),                     0) AS excluded_count
      FROM transactions
      WHERE owner_id = :ownerId
        AND date BETWEEN :rangeStart AND :rangeEnd
      """;

  private static final String TREND_SQL = """
      SELECT
          date_trunc('month', date)::date                                                   AS month_start,
          COALESCE(SUM(amount) FILTER (WHERE currency = :currency AND type = 'INCOME'),  0) AS income,
          COALESCE(SUM(amount) FILTER (WHERE currency = :currency AND type = 'EXPENSE'), 0) AS expense,
          COALESCE(COUNT(*)    FILTER (WHERE currency = :currency AND type = 'INCOME'),  0) AS income_count,
          COALESCE(COUNT(*)    FILTER (WHERE currency = :currency AND type = 'EXPENSE'), 0) AS expense_count,
          COALESCE(COUNT(*)    FILTER (WHERE currency <> :currency),                     0) AS excluded_count
      FROM transactions
      WHERE owner_id = :ownerId
        AND date BETWEEN :rangeStart AND :rangeEnd
      GROUP BY 1
      """;

  private final JdbcClient jdbcClient;

  public TransactionSummaryRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  /**
   * Aggregate transactions across the inclusive {@code [rangeStart, rangeEnd]} date window into
   * a single row of income / expense totals and counts, scoped to the authenticated owner.
   * Transactions in other currencies are tallied into {@code excludedCount}.
   */
  public TransactionSummaryRow getSummary(
      UUID ownerId, LocalDate rangeStart, LocalDate rangeEnd, String currency) {

    return jdbcClient.sql(SUMMARY_SQL)
        .param(OWNER_ID, ownerId)
        .param(RANGE_START, rangeStart)
        .param(RANGE_END, rangeEnd)
        .param(CURRENCY, currency)
        .query(TRANSACTION_SUMMARY_ROW_ROW_MAPPER)
        .single();
  }

  /**
   * Aggregate transactions per calendar month across the range. Months with no
   * matching transactions are absent from the result; callers are responsible
   * for zero-filling so the response array length matches the requested range.
   */
  public Map<YearMonth, TransactionSummaryRow> getTrend(
      UUID ownerId, LocalDate rangeStart, LocalDate rangeEnd, String currency) {

    return jdbcClient.sql(TREND_SQL)
        .param(OWNER_ID, ownerId)
        .param(RANGE_START, rangeStart)
        .param(RANGE_END, rangeEnd)
        .param(CURRENCY, currency)
        .query(TREND_BUCKET_ROW_MAPPER)
        .stream()
        .collect(Collectors.toUnmodifiableMap(TransactionTrendBucket::month, TransactionTrendBucket::row));
  }

}
