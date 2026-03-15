package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for Transaction entity operations using Spring Data JDBC.
 */
public interface TransactionRepository extends BaseRepository<TransactionEntity, UUID> {

  String FIND_ALL_BY_FILTERS_ORDER_BY_DATE_DESC = """
      SELECT * FROM transactions
      WHERE (:startDate::date IS NULL OR date >= :startDate::date)
      AND (:endDate::date IS NULL OR date <= :endDate::date)
      AND (:categoryId::uuid IS NULL OR category_id = :categoryId::uuid)
      ORDER BY date DESC
      LIMIT :limit
      OFFSET :offset
      """;

  String FIND_ALL_BY_FILTERS_ORDER_BY_DATE_ASC = """
      SELECT * FROM transactions
      WHERE (:startDate::date IS NULL OR date >= :startDate::date)
      AND (:endDate::date IS NULL OR date <= :endDate::date)
      AND (:categoryId::uuid IS NULL OR category_id = :categoryId::uuid)
      ORDER BY date ASC
      LIMIT :limit
      OFFSET :offset
      """;

  default List<TransactionEntity> findAllByFilters(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("categoryId") UUID categoryId,
      Pageable pageable
  ) {
    var order = Optional.of(pageable.getSort())
        .map(s -> s.getOrderFor("date"))
        .map(Order::getDirection)
        .orElse(Direction.DESC);

    return order.isAscending()
        ? findAllByFiltersOrderByDateAsc(startDate, endDate, categoryId, pageable.getPageSize(), pageable.getOffset())
        : findAllByFiltersOrderByDateDesc(startDate, endDate, categoryId, pageable.getPageSize(), pageable.getOffset());
  }

  @Query(FIND_ALL_BY_FILTERS_ORDER_BY_DATE_DESC)
  List<TransactionEntity> findAllByFiltersOrderByDateDesc(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("categoryId") UUID categoryId,
      @Param("limit") int limit,
      @Param("offset") long offset
  );

  @Query(FIND_ALL_BY_FILTERS_ORDER_BY_DATE_ASC)
  List<TransactionEntity> findAllByFiltersOrderByDateAsc(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("categoryId") UUID categoryId,
      @Param("limit") int limit,
      @Param("offset") long offset
  );

  @Query("SELECT COUNT(*) FROM transactions " +
      "WHERE (:startDate::date IS NULL OR date >= :startDate::date) " +
      "AND (:endDate::date IS NULL OR date <= :endDate::date) " +
      "AND (:categoryId::uuid IS NULL OR category_id = :categoryId::uuid)")
  long countByFilters(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("categoryId") UUID categoryId
  );
}
