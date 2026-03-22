package com.budget.buddy.budget_buddy_api.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.generated.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryUpdate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CategoryMapperTest {

  private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

  @Nested
  class ToEntity {

    @Test
    void shouldMapCategoryCreateToCategoryEntity() {
      // Given
      var create = new CategoryCreate("Groceries");

      // When
      var entity = categoryMapper.toEntity(create);

      // Then
      assertThat(entity)
          .as("Mapped entity should not be null")
          .isNotNull()
          .returns("Groceries", CategoryEntity::getName)
          .returns(null, CategoryEntity::getId);
    }
  }

  @Nested
  class ToModel {

    @Test
    void shouldMapCategoryEntityToCategory() {
      // Given
      var id = UUID.randomUUID();
      var now = OffsetDateTime.now();
      var entity = new CategoryEntity(id, "Groceries", UUID.randomUUID());
      entity.setCreatedAt(now);
      entity.setUpdatedAt(now);

      // When
      var model = categoryMapper.toModel(entity);

      // Then
      assertThat(model)
          .as("Mapped model should not be null")
          .isNotNull()
          .returns(id, com.budget.buddy.budget_buddy_api.generated.model.Category::getId)
          .returns("Groceries", com.budget.buddy.budget_buddy_api.generated.model.Category::getName)
          .returns(now, com.budget.buddy.budget_buddy_api.generated.model.Category::getCreatedAt)
          .returns(now, com.budget.buddy.budget_buddy_api.generated.model.Category::getUpdatedAt);
    }
  }

  @Nested
  class ToModelList {

    @Test
    void shouldMapEntitiesToModels() {
      // Given
      var entity1 = new CategoryEntity(UUID.randomUUID(), "Cat 1", UUID.randomUUID());
      var entity2 = new CategoryEntity(UUID.randomUUID(), "Cat 2", UUID.randomUUID());

      // When
      var models = categoryMapper.toModelList(List.of(entity1, entity2));

      // Then
      assertThat(models).hasSize(2);
      assertThat(models.get(0).getName()).isEqualTo("Cat 1");
      assertThat(models.get(1).getName()).isEqualTo("Cat 2");
    }
  }

  @Nested
  class PatchEntity {

    @Test
    void shouldUpdateOnlyProvidedFields() {
      // Given
      var entity = new CategoryEntity(UUID.randomUUID(), "Old Name", UUID.randomUUID());
      var update = new CategoryUpdate();
      update.setName("New Name");

      // When
      categoryMapper.patchEntity(update, entity);

      // Then
      assertThat(entity.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldNotUpdateIfNull() {
      // Given
      var entity = new CategoryEntity(UUID.randomUUID(), "Keep Me", UUID.randomUUID());
      var update = new CategoryUpdate();
      update.setName(null);

      // When
      categoryMapper.patchEntity(update, entity);

      // Then
      assertThat(entity.getName()).isEqualTo("Keep Me");
    }
  }
}
