package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for Transaction entity operations using Spring Data JDBC.
 */
public interface TransactionRepository extends BaseRepository<TransactionEntity, UUID> {

  /**
   * Find transactions by date range and optional category.
   *
   * @param startDate start date inclusive (optional)
   * @param endDate end date inclusive (optional)
   * @param categoryId category ID (optional)
   * @return list of matching transactions
   */
  @Query("SELECT * FROM transactions " +
      "WHERE (:startDate IS NULL OR date >= :startDate) " +
      "AND (:endDate IS NULL OR date <= :endDate) " +
      "AND (:categoryId IS NULL OR category_id = :categoryId)")
  List<TransactionEntity> findByDateRangeAndCategory(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("categoryId") UUID categoryId
  );
}
