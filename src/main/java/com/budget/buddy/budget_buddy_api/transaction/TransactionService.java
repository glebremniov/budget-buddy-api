package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.AbstractBaseService;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TransactionService extends
    AbstractBaseService<TransactionEntity, UUID, Transaction, TransactionCreate, TransactionUpdate> {

  private final TransactionRepository repository;
  private final TransactionMapper mapper;

  public TransactionService(TransactionRepository repository, TransactionMapper mapper) {
    super(repository, mapper);
    this.repository = repository;
    this.mapper = mapper;
  }

  public List<Transaction> list(int limit, int offset, UUID categoryId, LocalDate start, LocalDate end) {
    var entities = repository.findByDateRangeAndCategory(start, end, categoryId);
    var endIdx = Math.min(offset + limit, entities.size());
    var page = entities.subList(offset, endIdx);
    return mapper.toModelList(page);
  }

  public long count(UUID categoryId, LocalDate start, LocalDate end) {
    return repository.findByDateRangeAndCategory(start, end, categoryId).size();
  }
}
