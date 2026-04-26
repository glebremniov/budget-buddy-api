package com.budget.buddy.budget_buddy_api.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository fragment for filtered transaction queries.
 * Allows dynamic sort direction and optional filters without
 * duplicating SQL per ORDER BY variant.
 */
public interface TransactionPagingRepository {

  Page<TransactionEntity> findAllByFilter(TransactionFilter filter, Pageable pageable);

}
