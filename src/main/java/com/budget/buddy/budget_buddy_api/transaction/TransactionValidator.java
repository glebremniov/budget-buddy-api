package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.category.CategoryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionValidator implements BaseEntityValidator<TransactionEntity> {

  private final CategoryService categoryService;

  @Override
  public void validate(TransactionEntity entity) {
    validateCategory(entity.getCategoryId());
  }

  private void validateCategory(UUID categoryId) {
    if (categoryId == null) {
      throw new IllegalArgumentException("Category ID must be set");
    }

    if (!categoryService.existsById(categoryId)) {
      throw new IllegalArgumentException("Unknown category with id: " + categoryId);
    }
  }
}
