package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.AbstractCRUDLController;
import com.budget.buddy.budget_buddy_api.generated.api.TransactionsApi;
import com.budget.buddy.budget_buddy_api.generated.model.PaginationMeta;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import com.budget.buddy.budget_buddy_api.generated.model.V1TransactionsGet200Response;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transaction controller for CRUDL operations on transactions.
 */
@RestController
public class TransactionController
    extends AbstractCRUDLController<TransactionEntity, UUID, Transaction, TransactionCreate, TransactionUpdate, V1TransactionsGet200Response>
    implements TransactionsApi {

  private final TransactionService service;

  public TransactionController(TransactionService service) {
    super(service);
    this.service = service;
  }

  @Override
  public ResponseEntity<V1TransactionsGet200Response> v1TransactionsGet(Integer limit, Integer offset, String categoryId, LocalDate start, LocalDate end, String sort) {
    var categoryUUID = UUID.fromString(categoryId);
    var transactions = service.list(limit, offset, categoryUUID, start, end);
    var total = service.count(categoryUUID, start, end);

    var meta = new PaginationMeta();
    meta.setLimit(limit);
    meta.setOffset(offset);
    meta.setTotal((int) total);

    var response = new V1TransactionsGet200Response()
        .items(transactions)
        .meta(meta);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Transaction> v1TransactionsPost(TransactionCreate transactionCreate) {
    return super.createInternal(transactionCreate);
  }

  @Override
  public ResponseEntity<Void> v1TransactionsTransactionIdDelete(String transactionId) {
    return super.deleteInternal(transactionId);
  }

  @Override
  public ResponseEntity<Transaction> v1TransactionsTransactionIdGet(String transactionId) {
    return super.readInternal(transactionId);
  }

  @Override
  public ResponseEntity<Transaction> v1TransactionsTransactionIdPut(String transactionId, TransactionUpdate transactionUpdate) {
    return super.updateInternal(transactionId, transactionUpdate);
  }

  @Override
  protected URI createdURI(Transaction created) {
    return URI.create("/v1/transactions/" + created.getId());
  }

  @Override
  protected V1TransactionsGet200Response listResponse(List<Transaction> items, PaginationMeta meta) {
    return new V1TransactionsGet200Response()
        .items(items)
        .meta(meta);
  }

  @Override
  protected UUID fromString(String id) {
    return UUID.fromString(id);
  }
}
