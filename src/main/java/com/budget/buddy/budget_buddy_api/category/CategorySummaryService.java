package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategorySpendingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategorySummaryService {

  private final CategorySummaryRepository repository;
  private final CategoryMapper mapper;
  private final OwnerIdProvider<UUID> ownerIdProvider;

  public CategorySpendingSummary getSummary(String month, String currency) {
    var yearMonth = parseMonth(month);
    var rows = repository.getSummary(
            ownerIdProvider.get(), yearMonth.atDay(1), yearMonth.atEndOfMonth(), currency)
        .stream()
        .map(mapper::toSpendingRow)
        .toList();
    return new CategorySpendingSummary(month, currency, rows);
  }

  private static YearMonth parseMonth(String month) {
    try {
      return YearMonth.parse(month);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid month format: " + month, e);
    }
  }

}
