package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.category.CategoryService;
import java.util.Currency;
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
    validateAmount(entity.getAmount());
    validateCurrency(entity.getCurrency());
  }

  private void validateCategory(UUID categoryId) {
    if (categoryId == null) {
      throw new IllegalArgumentException("Category ID must be set");
    }

    if (!categoryService.existsById(categoryId)) {
      throw new IllegalArgumentException("Unknown category with id: " + categoryId);
    }
  }

  private void validateAmount(Integer amount) {
    if (amount == null || amount < 1) {
      throw new IllegalArgumentException("Amount must be a positive value in minor units");
    }
  }

  private void validateCurrency(Currency currency) {
    if (currency == null) {
      throw new IllegalArgumentException("Currency must be set");
    }
  }
}
