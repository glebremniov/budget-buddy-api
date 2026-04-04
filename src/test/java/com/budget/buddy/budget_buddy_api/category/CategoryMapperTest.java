package com.budget.buddy.budget_buddy_api.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.generated.model.Category;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryWrite;
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
    void should_MapCategoryWriteToCategoryEntity() {
      // Given
      var create = new CategoryWrite("Groceries");

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
    void should_MapCategoryEntityToCategory() {
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
          .returns(id, Category::getId)
          .returns("Groceries", Category::getName)
          .returns(now, Category::getCreatedAt)
          .returns(now, Category::getUpdatedAt);
    }
  }

  @Nested
  class ToModelList {

    @Test
    void should_MapEntitiesToModels() {
      // Given
      var id1 = UUID.randomUUID();
      var id2 = UUID.randomUUID();
      var entity1 = new CategoryEntity(id1, "Cat 1", UUID.randomUUID());
      var entity2 = new CategoryEntity(id2, "Cat 2", UUID.randomUUID());

      // When
      var models = categoryMapper.toModelList(List.of(entity1, entity2));

      // Then
      assertThat(models)
          .as("Mapped model list should have correct size and elements")
          .hasSize(2);

      assertThat(models.get(0))
          .as("First model should match first entity")
          .returns(id1, Category::getId)
          .returns("Cat 1", Category::getName);

      assertThat(models.get(1))
          .as("Second model should match second entity")
          .returns(id2, Category::getId)
          .returns("Cat 2", Category::getName);
    }
  }

  @Nested
  class PatchEntity {

    @Test
    void should_UpdateOnlyProvidedFields() {
      // Given
      var originalId = UUID.randomUUID();
      var ownerId = UUID.randomUUID();
      var entity = new CategoryEntity(originalId, "Old Name", ownerId);
      var update = new CategoryUpdate();
      update.setName("New Name");

      // When
      categoryMapper.patchEntity(update, entity);

      // Then
      assertThat(entity)
          .as("Entity name should be updated while other fields remain unchanged")
          .returns("New Name", CategoryEntity::getName)
          .returns(originalId, CategoryEntity::getId)
          .returns(ownerId, CategoryEntity::getOwnerId);
    }

    @Test
    void should_NotUpdateIfNull() {
      // Given
      var originalName = "Keep Me";
      var entity = new CategoryEntity(UUID.randomUUID(), originalName, UUID.randomUUID());
      var update = new CategoryUpdate();
      update.setName(null);

      // When
      categoryMapper.patchEntity(update, entity);

      // Then
      assertThat(entity.getName())
          .as("Entity name should remain unchanged when the update request contains null")
          .isEqualTo(originalName);
    }
  }
}
