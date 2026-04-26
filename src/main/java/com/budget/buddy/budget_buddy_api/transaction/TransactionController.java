package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityController;
import com.budget.buddy.budget_buddy_contracts.generated.api.TransactionsApi;
import com.budget.buddy.budget_buddy_contracts.generated.model.*;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

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
      Integer page, Integer size,
      UUID categoryId, LocalDate start, LocalDate end,
      TransactionType type, String sort
  ) {
    var pageable = PageRequest.of(page, size, buildSort(sort));
    var filter = TransactionFilter.of(categoryId, start, end, mapper.toModel(type));
    return listTransactions(filter, pageable);
  }

  ResponseEntity<PaginatedTransactions> listTransactions(TransactionFilter filter, Pageable pageable) {
    var items = service.list(filter, pageable);

    var meta = new PaginationMeta();
    meta.setPage(items.getNumber());
    meta.setSize(items.getSize());
    meta.setTotal(items.getTotalElements());

    return ResponseEntity.ok(mapper.toPageResponse(items.getContent(), meta));
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

  private static Sort buildSort(String sortStr) {
    return Direction.fromOptionalString(sortStr)
        .map(direction -> Sort.by(direction, "date"))
        .orElse(DEFAULT_SORT);
  }
}
