package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction entity operations using Spring Data JDBC.
 */
public interface TransactionRepository extends OwnableEntityRepository<TransactionEntity, UUID> {

  String FIND_ALL_BY_FILTERS_WHERE_CLAUSE = """
      SELECT * FROM transactions
      WHERE owner_id = :ownerId::uuid
      AND (:startDate::date IS NULL OR date >= :startDate::date)
      AND (:endDate::date IS NULL OR date <= :endDate::date)
      AND (:categoryId::uuid IS NULL OR category_id = :categoryId::uuid)
      AND (:type::text IS NULL OR type = :type::text)
      """;

  String FIND_ALL_BY_FILTERS_ORDER_BY_DATE_DESC_QUERY =
      FIND_ALL_BY_FILTERS_WHERE_CLAUSE + """
          ORDER BY date DESC, created_at DESC
          LIMIT :limit OFFSET :offset
          """;

  String FIND_ALL_BY_FILTERS_ORDER_BY_DATE_ASC_QUERY =
      FIND_ALL_BY_FILTERS_WHERE_CLAUSE + """
          ORDER BY date, created_at
          LIMIT :limit OFFSET :offset
          """;

  String COUNT_BY_FILTERS_QUERY = """
      SELECT COUNT(*) FROM transactions
      WHERE owner_id = :ownerId::uuid
      AND (:startDate::date IS NULL OR date >= :startDate::date)
      AND (:endDate::date IS NULL OR date <= :endDate::date)
      AND (:categoryId::uuid IS NULL OR category_id = :categoryId::uuid)
      AND (:type::text IS NULL OR type = :type::text)
      """;

  default List<TransactionEntity> findAllByFilter(
      TransactionFilter filter,
      Pageable pageable
  ) {
    var order = Optional.of(pageable.getSort())
        .map(s -> s.getOrderFor("date"))
        .map(Order::getDirection)
        .orElse(Direction.DESC);

    var type = Optional.ofNullable(filter.type())
        .map(Enum::name)
        .orElse(null);

    if (order.isAscending()) {
      return findAllByFilterOrderByDateAsc(
          filter.ownerId(),
          filter.start(),
          filter.end(),
          filter.categoryId(),
          type,
          pageable.getPageSize(),
          pageable.getOffset());
    }

    return findAllByFilterOrderByDateDesc(
        filter.ownerId(),
        filter.start(),
        filter.end(),
        filter.categoryId(),
        type,
        pageable.getPageSize(),
        pageable.getOffset());
  }

  @Query(FIND_ALL_BY_FILTERS_ORDER_BY_DATE_DESC_QUERY)
  List<TransactionEntity> findAllByFilterOrderByDateDesc(
      @Param("ownerId") UUID ownerId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("categoryId") UUID categoryId,
      @Param("type") String type,
      @Param("limit") int limit,
      @Param("offset") long offset
  );

  @Query(FIND_ALL_BY_FILTERS_ORDER_BY_DATE_ASC_QUERY)
  List<TransactionEntity> findAllByFilterOrderByDateAsc(
      @Param("ownerId") UUID ownerId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("categoryId") UUID categoryId,
      @Param("type") String type,
      @Param("limit") int limit,
      @Param("offset") long offset
  );

  default long countByFilter(TransactionFilter filter) {
    var type = Optional.ofNullable(filter.type())
        .map(Enum::name)
        .orElse(null);
    return countByFilter(filter.ownerId(), filter.start(), filter.end(), filter.categoryId(), type);
  }

  @Query(COUNT_BY_FILTERS_QUERY)
  long countByFilter(
      @Param("ownerId") UUID ownerId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("categoryId") UUID categoryId,
      @Param("type") String type
  );
}
