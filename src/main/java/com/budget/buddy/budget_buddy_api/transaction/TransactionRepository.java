package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction entity operations using Spring Data JDBC.
 */
@Repository
public interface TransactionRepository extends BaseRepository<TransactionEntity, UUID>, CrudRepository<TransactionEntity, UUID> {

  /**
   * Find all transactions.
   *
   * @return list of all transactions
   */
  List<TransactionEntity> findAll();

  /**
   * Find a transaction by ID.
   *
   * @param id transaction ID
   * @return optional containing the transaction if found
   */
  Optional<TransactionEntity> findById(UUID id);

  /**
   * Find transactions by category ID.
   *
   * @param categoryId category ID
   * @return list of transactions in the category
   */
  List<TransactionEntity> findByCategoryId(UUID categoryId);

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
