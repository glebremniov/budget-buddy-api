package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.MonthlySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionSummaryService {

  private final TransactionSummaryRepository repository;
  private final OwnerIdProvider<UUID> ownerIdProvider;

  public MonthlySummary getSummary(String month, String currency) {
    var yearMonth = parseMonth(month);
    var row = repository.getSummary(
        ownerIdProvider.get(), yearMonth.atDay(1), yearMonth.atEndOfMonth(), currency);
    return new MonthlySummary(
        month,
        currency,
        row.income(),
        row.expense(),
        row.income() - row.expense(),
        row.incomeCount(),
        row.expenseCount(),
        row.excludedCount()
    );
  }

  private static YearMonth parseMonth(String month) {
    try {
      return YearMonth.parse(month);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid month format: " + month, e);
    }
  }

}
