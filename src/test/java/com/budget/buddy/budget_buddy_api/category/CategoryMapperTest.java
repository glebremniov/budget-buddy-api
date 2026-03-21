package com.budget.buddy.budget_buddy_api.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.generated.model.Category;
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
      CategoryCreate create = new CategoryCreate("Groceries");

      // When
      CategoryEntity entity = categoryMapper.toEntity(create);

      // Then
      assertThat(entity).isNotNull();
      assertThat(entity.getName()).isEqualTo("Groceries");
      assertThat(entity.getId()).isNull();
    }
  }

  @Nested
  class ToModel {

    @Test
    void shouldMapCategoryEntityToCategory() {
      // Given
      UUID id = UUID.randomUUID();
      OffsetDateTime now = OffsetDateTime.now();
      CategoryEntity entity = new CategoryEntity(id, "Groceries", UUID.randomUUID());
      entity.setCreatedAt(now);
      entity.setUpdatedAt(now);

      // When
      Category model = categoryMapper.toModel(entity);

      // Then
      assertThat(model).isNotNull();
      assertThat(model.getId()).isEqualTo(id);
      assertThat(model.getName()).isEqualTo("Groceries");
      assertThat(model.getCreatedAt()).isEqualTo(now);
      assertThat(model.getUpdatedAt()).isEqualTo(now);
    }
  }

  @Nested
  class ToModelList {

    @Test
    void shouldMapEntitiesToModels() {
      // Given
      CategoryEntity entity1 = new CategoryEntity(UUID.randomUUID(), "Cat 1", UUID.randomUUID());
      CategoryEntity entity2 = new CategoryEntity(UUID.randomUUID(), "Cat 2", UUID.randomUUID());

      // When
      List<Category> models = categoryMapper.toModelList(List.of(entity1, entity2));

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
      CategoryEntity entity = new CategoryEntity(UUID.randomUUID(), "Old Name", UUID.randomUUID());
      CategoryUpdate update = new CategoryUpdate();
      update.setName("New Name");

      // When
      categoryMapper.patchEntity(update, entity);

      // Then
      assertThat(entity.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldNotUpdateIfNull() {
      // Given
      CategoryEntity entity = new CategoryEntity(UUID.randomUUID(), "Keep Me", UUID.randomUUID());
      CategoryUpdate update = new CategoryUpdate();
      update.setName(null);

      // When
      categoryMapper.patchEntity(update, entity);

      // Then
      assertThat(entity.getName()).isEqualTo("Keep Me");
    }
  }
}
