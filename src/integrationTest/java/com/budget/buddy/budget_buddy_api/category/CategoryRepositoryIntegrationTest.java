package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.BaseOwnableIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryRepository Integration Tests")
class CategoryRepositoryIntegrationTest extends BaseOwnableIntegrationTest {

  @Autowired
  private CategoryRepository categoryRepository;

  private UUID ownerId;

  @BeforeEach
  void setUp() {
    ownerId = upsertRandomUser();
  }

  @Test
  @DisplayName("should save and find category by ID and owner ID")
  void shouldSaveAndFindByIdAndOwnerId() {
    // Given
    var category = new CategoryEntity();
    category.setName("Food");
    category.setOwnerId(ownerId);
    var saved = categoryRepository.save(category);

    // When
    var found = categoryRepository.findByIdAndOwnerId(saved.getId(), ownerId);
    var notFound = categoryRepository.findByIdAndOwnerId(saved.getId(), UUID.randomUUID());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Food");
    assertThat(notFound).isEmpty();
  }

  @Test
  @DisplayName("should list categories by owner ID")
  void shouldFindAllByOwnerId() {
    // Given
    var c1 = new CategoryEntity();
    c1.setName("Food");
    c1.setOwnerId(ownerId);
    categoryRepository.save(c1);

    var c2 = new CategoryEntity();
    c2.setName("Rent");
    c2.setOwnerId(ownerId);
    categoryRepository.save(c2);

    var otherOwnerId = upsertRandomUser();
    var c3 = new CategoryEntity();
    c3.setName("Other");
    c3.setOwnerId(otherOwnerId);
    categoryRepository.save(c3);

    // When
    var page = categoryRepository.findAllByOwnerId(ownerId, PageRequest.of(0, 10));

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getContent()).extracting(CategoryEntity::getName).containsExactlyInAnyOrder("Food", "Rent");
  }

  @Test
  @DisplayName("should count categories by owner ID")
  void shouldCountByOwnerId() {
    // Given
    var c1 = new CategoryEntity();
    c1.setName("C1");
    c1.setOwnerId(ownerId);
    categoryRepository.save(c1);

    // When
    long count = categoryRepository.countByOwnerId(ownerId);
    long otherCount = categoryRepository.countByOwnerId(UUID.randomUUID());

    // Then
    assertThat(count).isEqualTo(1);
    assertThat(otherCount).isZero();
  }

  @Test
  @DisplayName("should check if category exists by ID and owner ID")
  void shouldCheckExistsByIdAndOwnerId() {
    // Given
    var category = new CategoryEntity();
    category.setName("Exists");
    category.setOwnerId(ownerId);
    var saved = categoryRepository.save(category);

    // When
    boolean exists = categoryRepository.existsByIdAndOwnerId(saved.getId(), ownerId);
    boolean notExists = categoryRepository.existsByIdAndOwnerId(saved.getId(), UUID.randomUUID());

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }
}
