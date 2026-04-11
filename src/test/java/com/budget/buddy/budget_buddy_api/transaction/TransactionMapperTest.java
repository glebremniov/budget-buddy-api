package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionUpdate;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.openapitools.jackson.nullable.JsonNullable;

class TransactionMapperTest {

  private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);

  @Nested
  class ToEntity {

    @Test
    void should_MapTransactionWriteToTransactionEntity() {
      // Given
      var categoryId = UUID.randomUUID();
      var date = LocalDate.now();
      var create = new TransactionWrite(
          categoryId,
          1000L,
          TransactionWrite.TypeEnum.EXPENSE,
          "EUR",
          date
      );
      create.setDescription("Test transaction");

      // When
      var entity = transactionMapper.toEntity(create);

      // Then
      assertThat(entity)
          .as("Mapped transaction entity should have correct values")
          .isNotNull()
          .returns(categoryId, TransactionEntity::getCategoryId)
          .returns(1000L, TransactionEntity::getAmount)
          .returns(TransactionType.EXPENSE, TransactionEntity::getType)
          .returns(Currency.getInstance("EUR"), TransactionEntity::getCurrency)
          .returns(date, TransactionEntity::getDate)
          .returns("Test transaction", TransactionEntity::getDescription);
    }
  }

  @Nested
  class ToModel {

    @Test
    void should_MapTransactionEntityToTransaction() {
      // Given
      var id = UUID.randomUUID();
      var categoryId = UUID.randomUUID();
      var ownerId = UUID.randomUUID();
      var date = LocalDate.now();
      var now = OffsetDateTime.now();

      var entity = new TransactionEntity(
          id,
          categoryId,
          500L,
          TransactionType.INCOME,
          Currency.getInstance("USD"),
          date,
          "Income description",
          ownerId
      );
      entity.setCreatedAt(now);
      entity.setUpdatedAt(now);

      // When
      var model = transactionMapper.toModel(entity);

      // Then
      assertThat(model)
          .as("Mapped transaction model should have correct values")
          .isNotNull()
          .returns(id, Transaction::getId)
          .returns(categoryId, Transaction::getCategoryId)
          .returns(500L, Transaction::getAmount)
          .returns("INCOME", m -> m.getType().getValue())
          .returns("USD", Transaction::getCurrency)
          .returns(date, Transaction::getDate)
          .returns("Income description", Transaction::getDescription)
          .returns(now, Transaction::getCreatedAt)
          .returns(now, Transaction::getUpdatedAt);
    }
  }

  @Nested
  class ToModelList {

    @Test
    void should_MapEntitiesToModels() {
      // Given
      var id1 = UUID.randomUUID();
      var id2 = UUID.randomUUID();
      var e1 = new TransactionEntity(id1, UUID.randomUUID(), 100L, TransactionType.EXPENSE, Currency.getInstance("EUR"), LocalDate.now(), "D1", UUID.randomUUID());
      var e2 = new TransactionEntity(id2, UUID.randomUUID(), 200L, TransactionType.INCOME, Currency.getInstance("USD"), LocalDate.now(), "D2", UUID.randomUUID());

      // When
      var models = transactionMapper.toModelList(List.of(e1, e2));

      // Then
      assertThat(models)
          .as("Mapped model list should have correct size and elements")
          .hasSize(2);

      assertThat(models.get(0))
          .as("First model should match first entity")
          .returns(id1, Transaction::getId)
          .returns(100L, Transaction::getAmount);

      assertThat(models.get(1))
          .as("Second model should match second entity")
          .returns(id2, Transaction::getId)
          .returns(200L, Transaction::getAmount);
    }
  }

  @Nested
  class PatchEntity {

    @Test
    void should_UpdateOnlyProvidedFields() {
      // Given
      var originalId = UUID.randomUUID();
      var categoryId = UUID.randomUUID();
      var ownerId = UUID.randomUUID();
      var date = LocalDate.now();
      var entity = new TransactionEntity(originalId, categoryId, 100L, TransactionType.EXPENSE, Currency.getInstance("EUR"), date, "Old Desc", ownerId);

      var update = new TransactionUpdate();
      update.setAmount(500L);
      update.setDescription(JsonNullable.of("New Desc"));

      // When
      transactionMapper.patchEntity(update, entity);

      // Then
      assertThat(entity)
          .as("Updated entity should have correct updated and original values")
          .returns(500L, TransactionEntity::getAmount)
          .returns("New Desc", TransactionEntity::getDescription)
          .returns(Currency.getInstance("EUR"), TransactionEntity::getCurrency)
          .returns(originalId, TransactionEntity::getId)
          .returns(categoryId, TransactionEntity::getCategoryId)
          .returns(date, TransactionEntity::getDate);
    }

    @Test
    void should_NotUpdateIfNull() {
      // Given
      var originalAmount = 100L;
      var originalDesc = "Keep Me";
      var entity = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), originalAmount, TransactionType.EXPENSE, Currency.getInstance("EUR"), LocalDate.now(), originalDesc, UUID.randomUUID());

      var update = new TransactionUpdate();
      update.setAmount(null);
      update.setDescription(null);

      // When
      transactionMapper.patchEntity(update, entity);

      // Then
      assertThat(entity)
          .as("Entity should remain unchanged when the update request contains nulls")
          .returns(originalAmount, TransactionEntity::getAmount)
          .returns(originalDesc, TransactionEntity::getDescription);
    }

    @Test
    void should_ClearDescription_When_ExplicitJsonNullableInPatch() {
      // Given
      var entity = new TransactionEntity(
          UUID.randomUUID(), UUID.randomUUID(), 100L, TransactionType.EXPENSE,
          Currency.getInstance("EUR"), LocalDate.now(), "Old Description", UUID.randomUUID());

      var update = new TransactionUpdate();
      update.setDescription(JsonNullable.undefined());

      // When
      transactionMapper.patchEntity(update, entity);

      // Then
      assertThat(entity.getDescription())
          .as("Description should be cleared when explicitly set to null in PATCH")
          .isNull();
    }
  }
}
