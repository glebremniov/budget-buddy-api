package com.budget.buddy.budget_buddy_api.category;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class CategorySummaryRepository {

  private static final String SUMMARY_SQL = """
      SELECT c.id    AS category_id,
             c.name  AS category_name,
             c.monthly_budget,
             COALESCE(SUM(t.amount) FILTER (WHERE t.currency = :currency), 0)  AS spent,
             COALESCE(COUNT(*)      FILTER (WHERE t.currency = :currency), 0)  AS transaction_count,
             COALESCE(COUNT(*)      FILTER (WHERE t.currency <> :currency), 0) AS excluded_transaction_count
      FROM categories c
      LEFT JOIN transactions t
             ON t.category_id = c.id
            AND t.owner_id    = :ownerId
            AND t.type        = 'EXPENSE'
            AND t.date BETWEEN :monthStart AND :monthEnd
      WHERE c.owner_id = :ownerId
      GROUP BY c.id
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
        .query((rs, _) -> new CategorySummaryRow(
            rs.getObject("category_id", UUID.class),
            rs.getString("category_name"),
            rs.getObject("monthly_budget", Long.class),
            rs.getLong("spent"),
            rs.getInt("transaction_count"),
            rs.getInt("excluded_transaction_count")
        ))
        .list();
  }

}
