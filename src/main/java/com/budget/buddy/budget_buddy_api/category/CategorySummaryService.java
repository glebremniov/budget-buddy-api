package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategorySpendingSummary;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CategorySummaryService {

  private final CategorySummaryRepository repository;
  private final CategoryMapper mapper;
  private final OwnerIdProvider<UUID> ownerIdProvider;

  public CategorySummaryService(
      CategorySummaryRepository repository,
      CategoryMapper mapper,
      OwnerIdProvider<UUID> ownerIdProvider
  ) {
    this.repository = repository;
    this.mapper = mapper;
    this.ownerIdProvider = ownerIdProvider;
  }

  public CategorySpendingSummary getSummary(String month, String currency) {
    YearMonth yearMonth;
    try {
      yearMonth = YearMonth.parse(month);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid month format: " + month, e);
    }
    var ownerId = ownerIdProvider.get();
    var rows = repository.getSummary(ownerId, yearMonth.atDay(1), yearMonth.atEndOfMonth(), currency)
        .stream()
        .map(mapper::toSpendingRow)
        .toList();
    return new CategorySpendingSummary(month, currency, rows);
  }

}
