package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityController;
import com.budget.buddy.budget_buddy_api.generated.api.TransactionsApi;
import com.budget.buddy.budget_buddy_api.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_api.generated.model.PaginationMeta;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionWrite;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transaction controller for CRUDL operations on transactions.
 */
@RestController
public class TransactionController
    extends BaseEntityController<UUID, Transaction, TransactionWrite, TransactionUpdate, PaginatedTransactions>
    implements TransactionsApi {

  private static final Sort DEFAULT_SORT = Sort.by(Direction.DESC, "date");

  private final TransactionService service;
  private final TransactionMapper mapper;

  public TransactionController(TransactionService service, TransactionMapper mapper) {
    super(service, mapper);
    this.service = service;
    this.mapper = mapper;
  }

  @Override
  public ResponseEntity<PaginatedTransactions> listTransactions(
      Integer limit,
      Integer offset,
      UUID categoryId,
      LocalDate start,
      LocalDate end,
      String order
  ) {
    var sort = Direction.fromOptionalString(order)
        .map(direction -> Sort.by(direction, "date"))
        .orElse(DEFAULT_SORT);

    var pageable = PageRequest.of(offset, limit, sort);
    var filter = TransactionFilter.of(categoryId, start, end);
    var items = service.list(filter, pageable);
    var total = service.count(filter);

    var meta = new PaginationMeta()
        .limit(limit)
        .offset(offset)
        .total(total);

    var response = mapper.toPageResponse(items, meta);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Transaction> createTransaction(TransactionWrite transactionCreate) {
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
  public ResponseEntity<Transaction> replaceTransaction(UUID transactionId, TransactionWrite transactionCreate) {
    return super.replaceInternal(transactionId, transactionCreate);
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
