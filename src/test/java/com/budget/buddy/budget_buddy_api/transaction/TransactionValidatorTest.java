package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.category.CategoryService;
import java.util.Currency;
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

  TransactionEntity validEntity(UUID categoryId) {
    var entity = new TransactionEntity();
    entity.setCategoryId(categoryId);
    entity.setAmount(1299);
    entity.setCurrency(Currency.getInstance("EUR"));
    return entity;
  }

  @Nested
  class Validate {

    @Test
    void should_PassValidation_When_CategoryExistsAndBelongsToCurrentUser() {
      // Given
      var categoryId = UUID.randomUUID();
      var entity = validEntity(categoryId);
      when(categoryService.existsById(categoryId)).thenReturn(true);

      // When & Then
      assertThatNoException()
          .as("Validation should pass when category exists and belongs to the user")
          .isThrownBy(() -> validator.validate(entity));
    }

    @Test
    void should_ThrowException_When_CategoryIdIsNull() {
      // Given
      var entity = validEntity(null);

      // When & Then
      assertThatThrownBy(() -> validator.validate(entity))
          .as("Should throw IllegalArgumentException when Category ID is null")
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Category ID must be set");
    }

    @Test
    void should_ThrowException_When_CategoryDoesNotExist() {
      // Given
      var categoryId = UUID.randomUUID();
      var entity = validEntity(categoryId);
      when(categoryService.existsById(categoryId)).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> validator.validate(entity))
          .as("Should throw IllegalArgumentException when the specified category does not exist")
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Unknown category with id: " + categoryId);
    }

    @Test
    void should_ThrowException_When_AmountIsNull() {
      // Given
      var categoryId = UUID.randomUUID();
      var entity = validEntity(categoryId);
      entity.setAmount(null);
      when(categoryService.existsById(categoryId)).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> validator.validate(entity))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Amount must be a positive value in minor units");
    }

    @Test
    void should_ThrowException_When_AmountIsZero() {
      // Given
      var categoryId = UUID.randomUUID();
      var entity = validEntity(categoryId);
      entity.setAmount(0);
      when(categoryService.existsById(categoryId)).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> validator.validate(entity))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Amount must be a positive value in minor units");
    }

    @Test
    void should_ThrowException_When_AmountIsNegative() {
      // Given
      var categoryId = UUID.randomUUID();
      var entity = validEntity(categoryId);
      entity.setAmount(-100);
      when(categoryService.existsById(categoryId)).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> validator.validate(entity))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Amount must be a positive value in minor units");
    }

    @Test
    void should_ThrowException_When_CurrencyIsNull() {
      // Given
      var categoryId = UUID.randomUUID();
      var entity = validEntity(categoryId);
      entity.setCurrency(null);
      when(categoryService.existsById(categoryId)).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> validator.validate(entity))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Currency must be set");
    }
  }
}
