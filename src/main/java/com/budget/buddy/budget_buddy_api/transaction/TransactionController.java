package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.AbstractCRUDLController;
import com.budget.buddy.budget_buddy_api.generated.api.TransactionsApi;
import com.budget.buddy.budget_buddy_api.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_api.generated.model.PaginationMeta;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transaction controller for CRUDL operations on transactions.
 */
@RestController
public class TransactionController
    extends AbstractCRUDLController<TransactionEntity, UUID, Transaction, TransactionCreate, TransactionUpdate, PaginatedTransactions, TransactionPatch>
    implements TransactionsApi {

  private final TransactionService service;
  private final TransactionMapper mapper;

  public TransactionController(TransactionService service, TransactionMapper mapper) {
    super(service, mapper);
    this.service = service;
    this.mapper = mapper;
  }

  @Override
  public ResponseEntity<PaginatedTransactions> listTransactions(
      Integer limit, Integer offset, UUID categoryId,
      LocalDate start, LocalDate end, String sort) {
    var items = service.list(limit, offset, categoryId, start, end);
    var total = service.count(categoryId, start, end);

    var meta = new PaginationMeta()
        .limit(limit)
        .offset(offset)
        .total((int) total);

    var response = mapper.toPageResponse(items, meta);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Transaction> createTransaction(TransactionCreate transactionCreate) {
    return super.createInternal(transactionCreate);
  }

  @Override
  public ResponseEntity<Void> deleteTransaction(UUID transactionId) {
    return super.deleteInternal(transactionId);
  }

  @Override
  public ResponseEntity<Transaction> getTransaction(UUID transactionId) {
    return super.readInternal(transactionId);
  }

  @Override
  public ResponseEntity<Transaction> updateTransaction(UUID transactionId, TransactionUpdate transactionUpdate) {
    return super.updateInternal(transactionId, transactionUpdate);
  }

  @Override
  protected URI createdURI(Transaction created) {
    return URI.create("/v1/transactions/" + created.getId());
  }

}
