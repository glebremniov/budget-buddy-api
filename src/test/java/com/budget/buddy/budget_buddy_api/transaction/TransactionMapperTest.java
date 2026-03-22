package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class TransactionMapperTest {

  private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);

  @Nested
  class ToEntity {

    @Test
    void shouldMapTransactionCreateToTransactionEntity() {
      // Given
      var categoryId = UUID.randomUUID();
      var create = new TransactionCreate(
          categoryId,
          1000,
          TransactionCreate.TypeEnum.EXPENSE,
          "EUR",
          LocalDate.now()
      );
      create.setDescription("Test transaction");

      // When
      var entity = transactionMapper.toEntity(create);

      // Then
      assertThat(entity).isNotNull();
      assertThat(entity.getCategoryId()).isEqualTo(categoryId);
      assertThat(entity.getAmount()).isEqualTo(1000);
      assertThat(entity.getType().name()).isEqualTo("EXPENSE");
      assertThat(entity.getCurrency()).isEqualTo("EUR");
      assertThat(entity.getDate()).isEqualTo(create.getDate());
      assertThat(entity.getDescription()).isEqualTo("Test transaction");
    }
  }

  @Nested
  class ToModel {

    @Test
    void shouldMapTransactionEntityToTransaction() {
      // Given
      var id = UUID.randomUUID();
      var categoryId = UUID.randomUUID();
      var ownerId = UUID.randomUUID();
      var date = LocalDate.now();
      var now = OffsetDateTime.now();

      var entity = new TransactionEntity(
          id,
          categoryId,
          500,
          TransactionType.INCOME,
          "USD",
          date,
          "Income description",
          ownerId
      );
      entity.setCreatedAt(now);
      entity.setUpdatedAt(now);

      // When
      var model = transactionMapper.toModel(entity);

      // Then
      assertThat(model).isNotNull();
      assertThat(model.getId()).isEqualTo(id);
      assertThat(model.getCategoryId()).isEqualTo(categoryId);
      assertThat(model.getAmount()).isEqualTo(500);
      assertThat(model.getType().getValue()).isEqualTo("INCOME");
      assertThat(model.getCurrency()).isEqualTo("USD");
      assertThat(model.getDate()).isEqualTo(date);
      assertThat(model.getDescription()).isEqualTo("Income description");
      assertThat(model.getCreatedAt()).isEqualTo(now);
      assertThat(model.getUpdatedAt()).isEqualTo(now);
    }
  }

  @Nested
  class ToModelList {

    @Test
    void shouldMapEntitiesToModels() {
      // Given
      var e1 = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), 100, TransactionType.EXPENSE, "EUR", LocalDate.now(), "D1", UUID.randomUUID());
      var e2 = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), 200, TransactionType.INCOME, "USD", LocalDate.now(), "D2", UUID.randomUUID());

      // When
      var models = transactionMapper.toModelList(List.of(e1, e2));

      // Then
      assertThat(models).hasSize(2);
      assertThat(models.get(0).getAmount()).isEqualTo(100);
      assertThat(models.get(1).getAmount()).isEqualTo(200);
    }
  }

  @Nested
  class PatchEntity {

    @Test
    void shouldUpdateOnlyProvidedFields() {
      // Given
      var entity = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), 100, TransactionType.EXPENSE, "EUR", LocalDate.now(), "Old Desc", UUID.randomUUID());
      var update = new TransactionUpdate();
      update.setAmount(500);
      update.setDescription("New Desc");

      // When
      transactionMapper.patchEntity(update, entity);

      // Then
      assertThat(entity.getAmount()).isEqualTo(500);
      assertThat(entity.getDescription()).isEqualTo("New Desc");
      assertThat(entity.getCurrency()).isEqualTo("EUR"); // Unchanged
    }

    @Test
    void shouldNotUpdateIfNull() {
      // Given
      var entity = new TransactionEntity(UUID.randomUUID(), UUID.randomUUID(), 100, TransactionType.EXPENSE, "EUR", LocalDate.now(), "Keep Me", UUID.randomUUID());
      var update = new TransactionUpdate();
      update.setAmount(null);
      update.setDescription(null);

      // When
      transactionMapper.patchEntity(update, entity);

      // Then
      assertThat(entity.getAmount()).isEqualTo(100);
      assertThat(entity.getDescription()).isEqualTo("Keep Me");
    }
  }
}
