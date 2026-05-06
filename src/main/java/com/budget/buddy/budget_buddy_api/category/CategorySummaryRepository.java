package com.budget.buddy.budget_buddy_api.category;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CategorySummaryRepository {

  private static final String SUMMARY_SQL = """
      SELECT c.id AS category_id, c.name AS category_name, c.monthly_budget,
             COALESCE(agg.spent, 0)      AS spent,
             COALESCE(agg.tx_count, 0)   AS transaction_count,
             COALESCE(other.tx_count, 0) AS excluded_transaction_count
      FROM categories c
      LEFT JOIN (
        SELECT category_id, SUM(amount) AS spent, COUNT(*) AS tx_count
        FROM transactions
        WHERE owner_id = :ownerId AND type = 'EXPENSE'
          AND currency = :currency
          AND date BETWEEN :monthStart AND :monthEnd
        GROUP BY category_id
      ) agg ON agg.category_id = c.id
      LEFT JOIN (
        SELECT category_id, COUNT(*) AS tx_count
        FROM transactions
        WHERE owner_id = :ownerId AND type = 'EXPENSE'
          AND currency <> :currency
          AND date BETWEEN :monthStart AND :monthEnd
        GROUP BY category_id
      ) other ON other.category_id = c.id
      WHERE c.owner_id = :ownerId
      ORDER BY c.name
      """;

  private final JdbcClient jdbcClient;

  public CategorySummaryRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public List<CategorySummaryRow> getSummary(
      UUID ownerId, LocalDate monthStart, LocalDate monthEnd, String currency) {

    return jdbcClient.sql(SUMMARY_SQL)
        .param("ownerId", ownerId)
        .param("monthStart", monthStart)
        .param("monthEnd", monthEnd)
        .param("currency", currency)
        .query((rs, _) -> {
          long budget = rs.getLong("monthly_budget");
          Long monthlyBudget = rs.wasNull() ? null : budget;
          return new CategorySummaryRow(
              (UUID) rs.getObject("category_id"),
              rs.getString("category_name"),
              monthlyBudget,
              rs.getLong("spent"),
              rs.getInt("transaction_count"),
              rs.getInt("excluded_transaction_count")
          );
        })
        .list();
  }

}
