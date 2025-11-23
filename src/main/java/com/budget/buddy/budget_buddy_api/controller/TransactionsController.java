package com.budget.buddy.budget_buddy_api.controller;

import com.budget.buddy.budget_buddy_api.model.PaginationMeta;
import com.budget.buddy.budget_buddy_api.model.Transaction;
import com.budget.buddy.budget_buddy_api.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.model.TransactionUpdate;
import com.budget.buddy.budget_buddy_api.service.TransactionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transaction controller for CRUDL operations on transactions.
 */
@RestController
public class TransactionsController {

  private final TransactionService transactionService;

  public TransactionsController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * List transactions with filtering
   *
   * @param limit pagination limit
   * @param offset pagination offset
   * @param categoryId optional category filter
   * @param start optional start date filter (YYYY-MM-DD)
   * @param end optional end date filter (YYYY-MM-DD)
   * @param sort optional sort parameter
   * @return list of transactions with pagination metadata
   */
  @GetMapping("/v1/transactions")
  public ResponseEntity<?> listTransactions(
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(required = false) String categoryId,
      @RequestParam(required = false) LocalDate start,
      @RequestParam(required = false) LocalDate end,
      @RequestParam(required = false) String sort) {

    var transactions = transactionService.listTransactions(limit, offset, categoryId, start, end);
    var total = transactionService.countTransactions(categoryId, start, end);

    var meta = new PaginationMeta();
    meta.setLimit(limit);
    meta.setOffset(offset);
    meta.setTotal((int) total);

    var response = new java.util.HashMap<>();
    response.put("items", transactions);
    response.put("meta", meta);

    return ResponseEntity.ok(response);
  }

  /**
   * Create a new transaction
   *
   * @param request transaction creation request
   * @return created transaction with 201 status
   */
  @PostMapping("/v1/transactions")
  public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionCreate request) {
    var created = transactionService.createTransaction(request);
    return ResponseEntity
        .created(URI.create("/v1/transactions/" + created.getId()))
        .body(created);
  }

  /**
   * Get a single transaction
   *
   * @param transactionId transaction ID
   * @return transaction details
   */
  @GetMapping("/v1/transactions/{transactionId}")
  public ResponseEntity<Transaction> getTransaction(@PathVariable String transactionId) {
    var transaction = transactionService.getTransaction(transactionId);
    return ResponseEntity.ok(transaction);
  }

  /**
   * Update a transaction
   *
   * @param transactionId transaction ID
   * @param request transaction update request
   * @return updated transaction
   */
  @PutMapping("/v1/transactions/{transactionId}")
  public ResponseEntity<Transaction> updateTransaction(
      @PathVariable String transactionId,
      @Valid @RequestBody TransactionUpdate request) {

    var updated = transactionService.updateTransaction(transactionId, request);
    return ResponseEntity.ok(updated);
  }

  /**
   * Delete a transaction
   *
   * @param transactionId transaction ID
   * @return 204 No Content
   */
  @DeleteMapping("/v1/transactions/{transactionId}")
  public ResponseEntity<Void> deleteTransaction(@PathVariable String transactionId) {
    transactionService.deleteTransaction(transactionId);
    return ResponseEntity.noContent().build();
  }
}
