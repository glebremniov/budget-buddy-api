package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategorySpendingSummary;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends OwnableEntityService<CategoryEntity, UUID, Category, CategoryWrite, CategoryUpdate> {

  private final CategoryMapper categoryMapper;
  private final CategorySummaryRepository summaryRepository;

  public CategoryService(
      CategoryRepository repository,
      CategoryMapper mapper,
      Set<BaseEntityValidator<CategoryEntity>> validators,
      OwnerIdProvider<UUID> ownerIdProvider,
      CategorySummaryRepository summaryRepository
  ) {
    super(repository, mapper, validators, ownerIdProvider);
    this.categoryMapper = mapper;
    this.summaryRepository = summaryRepository;
  }

  public CategorySpendingSummary getSummary(String month, String currency) {
    var ownerId = getOwnerIdProvider().get();
    var yearMonth = YearMonth.parse(month);
    var rows = summaryRepository.getSummary(ownerId, yearMonth.atDay(1), yearMonth.atEndOfMonth(), currency)
        .stream()
        .map(categoryMapper::toSpendingRow)
        .toList();
    return new CategorySpendingSummary(month, currency, rows);
  }

}
