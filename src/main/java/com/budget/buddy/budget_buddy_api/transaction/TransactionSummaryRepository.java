package com.budget.buddy.budget_buddy_api.transaction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public class TransactionSummaryRepository {

  private static final String SUMMARY_SQL = """
      SELECT
          COALESCE(SUM(amount) FILTER (WHERE currency = :currency AND type = 'INCOME'),  0) AS income,
          COALESCE(SUM(amount) FILTER (WHERE currency = :currency AND type = 'EXPENSE'), 0) AS expense,
          COALESCE(COUNT(*)    FILTER (WHERE currency = :currency AND type = 'INCOME'),  0) AS income_count,
          COALESCE(COUNT(*)    FILTER (WHERE currency = :currency AND type = 'EXPENSE'), 0) AS expense_count,
          COALESCE(COUNT(*)    FILTER (WHERE currency <> :currency),                     0) AS excluded_count
      FROM transactions
      WHERE owner_id = :ownerId
        AND date BETWEEN :monthStart AND :monthEnd
      """;

  private final JdbcClient jdbcClient;

  public TransactionSummaryRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public TransactionSummaryRow getSummary(
      UUID ownerId, LocalDate monthStart, LocalDate monthEnd, String currency) {

    return jdbcClient.sql(SUMMARY_SQL)
        .param("ownerId", ownerId)
        .param("monthStart", monthStart)
        .param("monthEnd", monthEnd)
        .param("currency", currency)
        .query((rs, _) -> new TransactionSummaryRow(
            rs.getLong("income"),
            rs.getLong("expense"),
            rs.getInt("income_count"),
            rs.getInt("expense_count"),
            rs.getInt("excluded_count")
        ))
        .single();
  }

}
