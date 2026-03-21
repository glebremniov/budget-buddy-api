package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.category.CategoryService;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionValidatorTest {

  @Mock
  CategoryService categoryService;

  @InjectMocks
  TransactionValidator validator;

  TransactionEntity entityWithCategory(UUID categoryId) {
    var entity = new TransactionEntity();
    entity.setCategoryId(categoryId);
    return entity;
  }

  @Nested
  class Validate {

    @Test
    void should_PassValidation_When_CategoryExistsAndBelongsToCurrentUser() {
      var categoryId = UUID.randomUUID();
      when(categoryService.existsById(categoryId)).thenReturn(true);

      assertThatNoException()
          .isThrownBy(() -> validator.validate(entityWithCategory(categoryId)));
    }

    @Test
    void should_Throw_When_CategoryIdIsNull() {
      var entity = entityWithCategory(null);
      assertThatThrownBy(() -> validator.validate(entity))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Category ID must be set");
    }

    @Test
    void should_Throw_When_CategoryDoesNotExist() {
      var categoryId = UUID.randomUUID();
      when(categoryService.existsById(categoryId)).thenReturn(false);

      var entity = entityWithCategory(categoryId);

      assertThatThrownBy(() -> validator.validate(entity))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Unknown category with id: " + categoryId);
    }
  }
}
