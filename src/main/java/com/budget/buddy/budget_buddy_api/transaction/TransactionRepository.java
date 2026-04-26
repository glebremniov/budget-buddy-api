package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Repository for Transaction entity operations using Spring Data JDBC.
 * Filtered queries with dynamic sort are provided via {@link TransactionPagingRepository}.
 */
public interface TransactionRepository
    extends OwnableEntityRepository<TransactionEntity, UUID>, TransactionPagingRepository {

  Page<TransactionEntity> findAllByFilter(TransactionFilter filter, Pageable pageable);

}
