package com.budget.buddy.budget_buddy_api.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.generated.model.Category;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryUpdate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository repository;
  @Mock
  private CategoryMapper mapper;

  private CategoryService categoryService;
  private final UUID currentUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    categoryService = new CategoryService(repository, mapper, Collections.emptySet(), () -> currentUserId);
  }

  @Nested
  class OwnableOperationTests {

    @Test
    void should_CreateCategory_WithOwnerId() {
      // Given
      var createRequest = new CategoryWrite("Groceries");
      var entity = new CategoryEntity();
      var model = new Category();

      when(mapper.toEntity(createRequest)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(entity);
      when(mapper.toModel(entity)).thenReturn(model);

      // When
      categoryService.create(createRequest);

      // Then
      assertThat(entity.getOwnerId())
          .as("Category owner ID should be set to the current user ID")
          .isEqualTo(currentUserId);

      verify(repository).save(entity);
    }

    @Test
    void should_ReadCategory_When_OwnedByUser() {
      // Given
      var categoryId = UUID.randomUUID();
      var entity = new CategoryEntity();
      var model = new Category();

      when(repository.findByIdAndOwnerId(categoryId, currentUserId)).thenReturn(Optional.of(entity));
      when(mapper.toModel(entity)).thenReturn(model);

      // When
      var result = categoryService.read(categoryId);

      // Then
      assertThat(result)
          .as("The returned category should match the expected model")
          .isNotNull()
          .isEqualTo(model);

      verify(repository).findByIdAndOwnerId(categoryId, currentUserId);
    }

    @Test
    void should_ThrowException_When_CategoryNotFoundOrNotOwned() {
      // Given
      var categoryId = UUID.randomUUID();
      when(repository.findByIdAndOwnerId(categoryId, currentUserId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> categoryService.read(categoryId))
          .as("Should throw EntityNotFoundException when the category is not found or not owned by user")
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void should_UpdateCategory_When_OwnedByUser() {
      // Given
      var categoryId = UUID.randomUUID();
      var updateRequest = new CategoryUpdate().name("New Name");
      var entity = new CategoryEntity();
      var model = new Category();

      when(repository.findByIdAndOwnerId(categoryId, currentUserId)).thenReturn(Optional.of(entity));
      when(repository.save(entity)).thenReturn(entity);
      when(mapper.toModel(entity)).thenReturn(model);

      // When
      var result = categoryService.update(categoryId, updateRequest);

      // Then
      assertThat(result)
          .as("Update result should match the mapped model")
          .isEqualTo(model);

      verify(mapper).patchEntity(updateRequest, entity);
      verify(repository).save(entity);
    }

    @Test
    void should_DeleteCategory_When_OwnedByUser() {
      // Given
      var categoryId = UUID.randomUUID();
      var entity = new CategoryEntity();
      when(repository.findByIdAndOwnerId(categoryId, currentUserId)).thenReturn(Optional.of(entity));

      // When
      categoryService.delete(categoryId);

      // Then
      verify(repository).delete(entity);
    }
  }
}
