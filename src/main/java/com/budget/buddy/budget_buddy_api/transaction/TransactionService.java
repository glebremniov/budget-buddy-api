package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionUpdate;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Service for managing user transactions. Extends {@link OwnableEntityService} to provide basic CRUD operations and includes custom filtering for transactions.
 */
@Transactional(readOnly = true)
@Service
public class TransactionService extends
    OwnableEntityService<TransactionEntity, UUID, Transaction, TransactionWrite, TransactionUpdate> {

  public TransactionService(
      TransactionRepository repository,
      TransactionMapper mapper,
      Set<BaseEntityValidator<TransactionEntity>> validators,
      OwnerIdProvider<UUID> ownerIdProvider
  ) {
    super(repository, mapper, validators, ownerIdProvider);
  }

  /**
   * List transactions with filtering and pagination.
   *
   * @param filter the transaction filter
   * @param pageable the page request
   * @return list of transactions
   */
  public Page<Transaction> list(TransactionFilter filter, Pageable pageable) {
    return getRepository()
        .findAllByFilter(filter.withOwnerId(getOwnerIdProvider().get()), pageable)
        .map(getMapper()::toModel);
  }

  @Override
  protected TransactionRepository getRepository() {
    return (TransactionRepository) super.getRepository();
  }

  @Override
  protected TransactionMapper getMapper() {
    return (TransactionMapper) super.getMapper();
  }

}
