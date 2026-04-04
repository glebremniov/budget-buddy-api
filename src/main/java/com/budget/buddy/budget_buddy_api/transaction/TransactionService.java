package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionUpdate;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service for managing user transactions. Extends {@link OwnableEntityService} to provide basic CRUD operations and includes custom filtering for transactions.
 */
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
  public List<Transaction> list(
      TransactionFilter filter,
      Pageable pageable
  ) {
    var entities = getRepository()
        .findAllByFilter(filter.withOwnerId(getOwnerIdProvider().get()), pageable);
    return getMapper()
        .toModelList(entities);
  }


  /**
   * Count transactions matching the provided filter.
   *
   * @param filter the transaction filter
   * @return the total count
   */
  public long count(TransactionFilter filter) {
    return getRepository()
        .countByFilter(filter.withOwnerId(getOwnerIdProvider().get()));
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
