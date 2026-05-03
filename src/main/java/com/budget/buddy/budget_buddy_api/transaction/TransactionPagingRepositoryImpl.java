package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.auditable.AuditableEntity;
import com.budget.buddy.budget_buddy_api.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
class TransactionPagingRepositoryImpl implements TransactionPagingRepository {

  private final JdbcAggregateOperations jdbc;
  private final CategoryRepository categoryRepository;

  @Override
  public Page<TransactionEntity> findAllByFilter(TransactionFilter filter, Pageable pageable) {
    var paging = withSecondarySort(pageable);
    var criteria = buildCriteria(filter);

    var entities = jdbc.findAll(Query.query(criteria).with(paging), TransactionEntity.class);

    return PageableExecutionUtils.getPage(
        entities,
        pageable,
        () -> jdbc.count(Query.query(criteria), TransactionEntity.class));
  }

  private static Pageable withSecondarySort(Pageable pageable) {
    var direction = pageable.getSort()
        .stream()
        .filter(o -> TransactionEntity.DATE.equals(o.getProperty()))
        .findFirst()
        .map(Order::getDirection)
        .orElse(Direction.DESC);

    return PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by(direction, TransactionEntity.DATE, AuditableEntity.CREATED_AT));
  }

  private Criteria buildCriteria(TransactionFilter filter) {
    var criteria = Criteria.where(TransactionEntity.OWNER_ID).is(filter.ownerId());

    if (filter.start() != null) {
      criteria = criteria.and(TransactionEntity.DATE).greaterThanOrEquals(filter.start());
    }
    if (filter.end() != null) {
      criteria = criteria.and(TransactionEntity.DATE).lessThanOrEquals(filter.end());
    }
    if (filter.categoryId() != null) {
      criteria = criteria.and(TransactionEntity.CATEGORY_ID).is(filter.categoryId());
    }
    if (filter.type() != null) {
      criteria = criteria.and(TransactionEntity.TYPE).is(filter.type());
    }
    if (filter.amountMin() != null) {
      criteria = criteria.and(TransactionEntity.AMOUNT).greaterThanOrEquals(filter.amountMin());
    }
    if (filter.amountMax() != null) {
      criteria = criteria.and(TransactionEntity.AMOUNT).lessThanOrEquals(filter.amountMax());
    }
    if (StringUtils.hasText(filter.query())) {
      criteria = criteria.and(searchCriteria(filter.ownerId(), filter.query()));
    }
    return criteria;
  }

  /**
   * Builds the {@code (description ILIKE ? OR category_id IN (...))} half of the WHERE clause.
   * Category matches are pre-fetched via a separate id-only query so we don't have to drop to
   * raw SQL with a JOIN.
   */
  private Criteria searchCriteria(UUID ownerId, String query) {
    var pattern = "%" + escapeLike(query) + "%";
    var byDescription = Criteria.where(TransactionEntity.DESCRIPTION).like(pattern).ignoreCase(true);

    var matchingCategoryIds = categoryRepository.findIdsByOwnerIdAndNameLike(ownerId, pattern);

    if (matchingCategoryIds.isEmpty()) {
      return byDescription;
    }

    return byDescription.or(Criteria.where(TransactionEntity.CATEGORY_ID).in(matchingCategoryIds));
  }

  /**
   * Escapes the SQL {@code LIKE} wildcards so user input matches literally.
   * PostgreSQL treats backslash as the default escape character for {@code LIKE}, so no
   * explicit {@code ESCAPE} clause is required.
   */
  private static String escapeLike(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
  }
}
