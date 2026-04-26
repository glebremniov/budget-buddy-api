package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.BaseOwnableIntegrationTest;
import com.budget.buddy.budget_buddy_api.category.CategoryEntity;
import com.budget.buddy.budget_buddy_api.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionRepository Integration Tests")
class TransactionRepositoryIntegrationTest extends BaseOwnableIntegrationTest {

  private static final Currency USD = Currency.getInstance("USD");
  private static final LocalDate TODAY = LocalDate.of(2026, 4, 26);

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  private UUID ownerId;
  private UUID categoryId;

  @BeforeEach
  void setUp() {
    ownerId = upsertRandomUser();
    categoryId = createCategory(ownerId, "Food");
  }

  @Test
  @DisplayName("should save and find transaction by id and owner id")
  void shouldSaveAndFindByIdAndOwnerId() {
    var saved = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 50L);

    var found = transactionRepository.findByIdAndOwnerId(saved.getId(), ownerId);

    assertThat(found).get()
        .returns(saved.getId(), TransactionEntity::getId)
        .returns(ownerId, TransactionEntity::getOwnerId)
        .returns(categoryId, TransactionEntity::getCategoryId)
        .returns(TODAY, TransactionEntity::getDate)
        .returns(TransactionType.EXPENSE, TransactionEntity::getType)
        .returns(50L, TransactionEntity::getAmount)
        .returns(USD, TransactionEntity::getCurrency);
  }

  @Nested
  @DisplayName("findAllByFilter")
  class FindAllByFilter {

    @Test
    @DisplayName("filters by start date inclusive")
    void filtersByStartDate() {
      var before = save(ownerId, categoryId, TODAY.minusDays(2), TransactionType.EXPENSE, 1L);
      var on = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 2L);
      var after = save(ownerId, categoryId, TODAY.plusDays(2), TransactionType.EXPENSE, 3L);

      var result = transactionRepository.findAllByFilter(
          new TransactionFilter(ownerId, null, TODAY, null, null),
          defaultPageable());

      assertThat(result.getContent())
          .extracting(TransactionEntity::getId)
          .containsExactlyInAnyOrder(on.getId(), after.getId())
          .doesNotContain(before.getId());
    }

    @Test
    @DisplayName("filters by end date inclusive")
    void filtersByEndDate() {
      var before = save(ownerId, categoryId, TODAY.minusDays(2), TransactionType.EXPENSE, 1L);
      var on = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 2L);
      var after = save(ownerId, categoryId, TODAY.plusDays(2), TransactionType.EXPENSE, 3L);

      var result = transactionRepository.findAllByFilter(
          new TransactionFilter(ownerId, null, null, TODAY, null),
          defaultPageable());

      assertThat(result.getContent())
          .extracting(TransactionEntity::getId)
          .containsExactlyInAnyOrder(before.getId(), on.getId())
          .doesNotContain(after.getId());
    }

    @Test
    @DisplayName("filters by category id")
    void filtersByCategoryId() {
      var otherCategoryId = createCategory(ownerId, "Travel");
      var matching = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 1L);
      save(ownerId, otherCategoryId, TODAY, TransactionType.EXPENSE, 2L);

      var result = transactionRepository.findAllByFilter(
          new TransactionFilter(ownerId, categoryId, null, null, null),
          defaultPageable());

      assertThat(result.getContent())
          .singleElement()
          .returns(matching.getId(), TransactionEntity::getId)
          .returns(categoryId, TransactionEntity::getCategoryId);
    }

    @Test
    @DisplayName("filters by type")
    void filtersByType() {
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 1L);
      var income = save(ownerId, categoryId, TODAY, TransactionType.INCOME, 2L);

      var result = transactionRepository.findAllByFilter(
          new TransactionFilter(ownerId, null, null, null, TransactionType.INCOME),
          defaultPageable());

      assertThat(result.getContent())
          .singleElement()
          .returns(income.getId(), TransactionEntity::getId)
          .returns(TransactionType.INCOME, TransactionEntity::getType);
    }

    @Test
    @DisplayName("excludes transactions owned by other users")
    void isolatesByOwner() {
      var otherOwnerId = upsertRandomUser();
      var otherOwnerCategoryId = createCategory(otherOwnerId, "Food");
      var mine = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 1L);
      save(otherOwnerId, otherOwnerCategoryId, TODAY, TransactionType.EXPENSE, 2L);

      var result = transactionRepository.findAllByFilter(
          new TransactionFilter(ownerId, null, null, null, null),
          defaultPageable());

      assertThat(result.getContent())
          .singleElement()
          .returns(mine.getId(), TransactionEntity::getId)
          .returns(ownerId, TransactionEntity::getOwnerId);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    @DisplayName("orders by date in the requested direction")
    void ordersByDate(Direction direction) {
      var oldest = save(ownerId, categoryId, TODAY.minusDays(2), TransactionType.EXPENSE, 1L);
      var middle = save(ownerId, categoryId, TODAY.minusDays(1), TransactionType.EXPENSE, 2L);
      var newest = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 3L);

      var result = transactionRepository.findAllByFilter(
          new TransactionFilter(ownerId, null, null, null, null),
          PageRequest.of(0, 10, direction, "date"));

      var ids = result.getContent().stream().map(TransactionEntity::getId).toList();
      if (direction == Direction.ASC) {
        assertThat(ids).containsExactly(oldest.getId(), middle.getId(), newest.getId());
      } else {
        assertThat(ids).containsExactly(newest.getId(), middle.getId(), oldest.getId());
      }
    }

    @Test
    @DisplayName("paginates results and reports total count")
    void paginates() {
      for (int i = 0; i < 5; i++) {
        save(ownerId, categoryId, TODAY.minusDays(i), TransactionType.EXPENSE, i);
      }

      var firstPage = transactionRepository.findAllByFilter(
          new TransactionFilter(ownerId, null, null, null, null),
          PageRequest.of(0, 2, Direction.DESC, "date"));
      var secondPage = transactionRepository.findAllByFilter(
          new TransactionFilter(ownerId, null, null, null, null),
          PageRequest.of(1, 2, Direction.DESC, "date"));

      assertThat(firstPage.getContent()).hasSize(2);
      assertThat(firstPage.getTotalElements()).isEqualTo(5);
      assertThat(firstPage.getTotalPages()).isEqualTo(3);
      assertThat(secondPage.getContent()).hasSize(2);
      assertThat(firstPage.getContent())
          .doesNotContainAnyElementsOf(secondPage.getContent());
    }

    @Test
    @DisplayName("returns empty page when no transactions match")
    void returnsEmptyPage() {
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 1L);

      var result = transactionRepository.findAllByFilter(
          new TransactionFilter(ownerId, null, TODAY.plusDays(10), null, null),
          defaultPageable());

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }
  }

  private UUID createCategory(UUID owner, String name) {
    var category = new CategoryEntity();
    category.setName(name);
    category.setOwnerId(owner);
    return categoryRepository.save(category).getId();
  }

  private TransactionEntity save(
      UUID owner, UUID category, LocalDate date, TransactionType type, long amount) {
    var entity = new TransactionEntity();
    entity.setOwnerId(owner);
    entity.setCategoryId(category);
    entity.setDate(date);
    entity.setType(type);
    entity.setAmount(amount);
    entity.setCurrency(USD);
    return transactionRepository.save(entity);
  }

  private static PageRequest defaultPageable() {
    return PageRequest.of(0, 10, Direction.DESC, "date");
  }
}
