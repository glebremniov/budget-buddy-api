package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityService;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TransactionService extends
    OwnableEntityService<TransactionEntity, UUID, Transaction, TransactionCreate, TransactionUpdate> {

  public TransactionService(
      TransactionRepository repository,
      TransactionMapper mapper,
      Set<BaseEntityValidator<TransactionEntity>> validators,
      Converter<String, UUID> ownerIdConverter
  ) {
    super(repository, mapper, validators, ownerIdConverter);
  }

  public List<Transaction> list(
      TransactionFilter filter,
      Pageable pageable
  ) {
    var entities = getRepository()
        .findAllByFilter(filter.withOwnerId(getRequiredOwnerId()), pageable);
    return getMapper()
        .toModelList(entities);
  }


  public long count(TransactionFilter filter) {
    return getRepository()
        .countByFilter(filter.withOwnerId(getRequiredOwnerId()));
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
