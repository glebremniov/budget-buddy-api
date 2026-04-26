package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.auditable.AuditableEntity;
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

@Transactional(readOnly = true)
@RequiredArgsConstructor
class TransactionPagingRepositoryImpl implements TransactionPagingRepository {

  private final JdbcAggregateOperations jdbc;

  @Override
  public Page<TransactionEntity> findAllByFilter(TransactionFilter filter, Pageable pageable) {
    var direction = pageable.getSort()
        .stream()
        .filter(o -> TransactionEntity.DATE.equals(o.getProperty()))
        .findFirst()
        .map(Order::getDirection)
        .orElse(Direction.DESC);

    var paging = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by(direction, TransactionEntity.DATE, AuditableEntity.CREATED_AT));

    var criteria = buildCriteria(filter);
    var entities = jdbc.findAll(Query.query(criteria).with(paging), TransactionEntity.class);

    return PageableExecutionUtils.getPage(
        entities,
        pageable,
        () -> jdbc.count(Query.query(criteria), TransactionEntity.class));
  }

  private static Criteria buildCriteria(TransactionFilter filter) {
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

    return criteria;
  }
}
