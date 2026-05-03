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
import org.junit.jupiter.params.provider.ValueSource;
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
          filter().withStart(TODAY).build(),
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
          filter().withEnd(TODAY).build(),
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
          filter().withCategoryId(categoryId).build(),
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
          filter().withType(TransactionType.INCOME).build(),
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
          filter().build(),
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
          filter().build(),
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
          filter().build(),
          PageRequest.of(0, 2, Direction.DESC, "date"));
      var secondPage = transactionRepository.findAllByFilter(
          filter().build(),
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
          filter().withStart(TODAY.plusDays(10)).build(),
          defaultPageable());

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("findAllByFilter — query (search)")
  class QuerySearch {

    @Test
    @DisplayName("matches transaction description (case-insensitive, partial)")
    void matchesDescription() {
      var coffee = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 1L, "Morning Coffee at the cafe");
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 2L, "Lunch");

      var result = transactionRepository.findAllByFilter(
          filter().withQuery("coffee").build(),
          defaultPageable());

      assertThat(result.getContent())
          .singleElement()
          .returns(coffee.getId(), TransactionEntity::getId);
    }

    @Test
    @DisplayName("matches category name when description does not match")
    void matchesCategoryName() {
      var travelCategoryId = createCategory(ownerId, "Travel");
      var match = save(ownerId, travelCategoryId, TODAY, TransactionType.EXPENSE, 1L, "Hotel booking");
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 2L, "Groceries");

      var result = transactionRepository.findAllByFilter(
          filter().withQuery("trav").build(),
          defaultPageable());

      assertThat(result.getContent())
          .singleElement()
          .returns(match.getId(), TransactionEntity::getId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"COFFEE", "coffee", "CoFfEe"})
    @DisplayName("query is case-insensitive against both description and category name")
    void caseInsensitive(String queryString) {
      var travelCategoryId = createCategory(ownerId, "Coffee Shops");
      var byDescription = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 1L, "morning coffee");
      var byCategory = save(ownerId, travelCategoryId, TODAY, TransactionType.EXPENSE, 2L, "tea");

      var result = transactionRepository.findAllByFilter(
          filter().withQuery(queryString).build(),
          defaultPageable());

      assertThat(result.getContent())
          .extracting(TransactionEntity::getId)
          .containsExactlyInAnyOrder(byDescription.getId(), byCategory.getId());
    }

    @Test
    @DisplayName("ignores transactions without a description when matching by category name")
    void matchesNullDescriptionViaCategory() {
      var travelCategoryId = createCategory(ownerId, "Travel");
      var match = save(ownerId, travelCategoryId, TODAY, TransactionType.EXPENSE, 1L, null);

      var result = transactionRepository.findAllByFilter(
          filter().withQuery("travel").build(),
          defaultPageable());

      assertThat(result.getContent())
          .singleElement()
          .returns(match.getId(), TransactionEntity::getId);
    }

    @Test
    @DisplayName("escapes LIKE wildcard characters")
    void escapesWildcards() {
      var literal = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 1L, "100% off");
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 2L, "50 off");

      var result = transactionRepository.findAllByFilter(
          filter().withQuery("100%").build(),
          defaultPageable());

      assertThat(result.getContent())
          .singleElement()
          .returns(literal.getId(), TransactionEntity::getId);
    }

    @Test
    @DisplayName("blank query is treated as no filter")
    void blankQueryNoOp() {
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 1L, "anything");
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 2L, "another");

      var result = transactionRepository.findAllByFilter(
          filter().withQuery("   ").build(),
          defaultPageable());

      assertThat(result.getContent()).hasSize(2);
    }
  }

  @Nested
  @DisplayName("findAllByFilter — amount range")
  class AmountRange {

    @Test
    @DisplayName("amountMin is inclusive lower bound")
    void amountMinInclusive() {
      var below = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 99L);
      var equal = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 100L);
      var above = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 101L);

      var result = transactionRepository.findAllByFilter(
          filter().withAmountMin(100L).build(),
          defaultPageable());

      assertThat(result.getContent())
          .extracting(TransactionEntity::getId)
          .containsExactlyInAnyOrder(equal.getId(), above.getId())
          .doesNotContain(below.getId());
    }

    @Test
    @DisplayName("amountMax is inclusive upper bound")
    void amountMaxInclusive() {
      var below = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 99L);
      var equal = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 100L);
      var above = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 101L);

      var result = transactionRepository.findAllByFilter(
          filter().withAmountMax(100L).build(),
          defaultPageable());

      assertThat(result.getContent())
          .extracting(TransactionEntity::getId)
          .containsExactlyInAnyOrder(below.getId(), equal.getId())
          .doesNotContain(above.getId());
    }

    @Test
    @DisplayName("both bounds form a closed range")
    void rangeBothBounds() {
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 99L);
      var inLow = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 100L);
      var inMid = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 150L);
      var inHigh = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 200L);
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 201L);

      var result = transactionRepository.findAllByFilter(
          filter().withAmountMin(100L).withAmountMax(200L).build(),
          defaultPageable());

      assertThat(result.getContent())
          .extracting(TransactionEntity::getId)
          .containsExactlyInAnyOrder(inLow.getId(), inMid.getId(), inHigh.getId());
    }

    @Test
    @DisplayName("amountMin == amountMax matches exactly")
    void exactAmount() {
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 99L);
      var exact = save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 100L);
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 101L);

      var result = transactionRepository.findAllByFilter(
          filter().withAmountMin(100L).withAmountMax(100L).build(),
          defaultPageable());

      assertThat(result.getContent())
          .singleElement()
          .returns(exact.getId(), TransactionEntity::getId);
    }
  }

  @Nested
  @DisplayName("findAllByFilter — combined filters")
  class CombinedFilters {

    @Test
    @DisplayName("query + amount range + date range + category combine with AND")
    void allFiltersCombined() {
      var travelCategoryId = createCategory(ownerId, "Travel");

      var match = save(ownerId, travelCategoryId, TODAY, TransactionType.EXPENSE, 150L, "Hotel in Paris");
      save(ownerId, travelCategoryId, TODAY, TransactionType.EXPENSE, 50L, "Hotel in Paris");
      save(ownerId, travelCategoryId, TODAY, TransactionType.EXPENSE, 150L, "Souvenir");
      save(ownerId, categoryId, TODAY, TransactionType.EXPENSE, 150L, "Hotel in Paris");
      save(ownerId, travelCategoryId, TODAY.minusDays(10), TransactionType.EXPENSE, 150L, "Hotel in Paris");
      save(ownerId, travelCategoryId, TODAY, TransactionType.INCOME, 150L, "Hotel in Paris");

      var result = transactionRepository.findAllByFilter(
          filter()
              .withQuery("hotel")
              .withAmountMin(100L)
              .withAmountMax(200L)
              .withCategoryId(travelCategoryId)
              .withStart(TODAY.minusDays(1))
              .withEnd(TODAY.plusDays(1))
              .withType(TransactionType.EXPENSE)
              .build(),
          defaultPageable());

      assertThat(result.getContent())
          .singleElement()
          .returns(match.getId(), TransactionEntity::getId);
    }

    @Test
    @DisplayName("does not match other users' categories when filtering by query")
    void queryRespectsOwnerScope() {
      var otherOwnerId = upsertRandomUser();
      var otherOwnerTravel = createCategory(otherOwnerId, "Travel");
      save(otherOwnerId, otherOwnerTravel, TODAY, TransactionType.EXPENSE, 1L, "Hotel");

      var result = transactionRepository.findAllByFilter(
          filter().withQuery("travel").build(),
          defaultPageable());

      assertThat(result.getContent()).isEmpty();
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
    return save(owner, category, date, type, amount, null);
  }

  private TransactionEntity save(
      UUID owner, UUID category, LocalDate date, TransactionType type, long amount, String description) {
    var entity = new TransactionEntity();
    entity.setOwnerId(owner);
    entity.setCategoryId(category);
    entity.setDate(date);
    entity.setType(type);
    entity.setAmount(amount);
    entity.setCurrency(USD);
    entity.setDescription(description);
    return transactionRepository.save(entity);
  }

  private FilterBuilder filter() {
    return new FilterBuilder(ownerId);
  }

  private static PageRequest defaultPageable() {
    return PageRequest.of(0, 10, Direction.DESC, "date");
  }

  private static final class FilterBuilder {
    private final UUID ownerId;
    private UUID categoryId;
    private LocalDate start;
    private LocalDate end;
    private TransactionType type;
    private String query;
    private Long amountMin;
    private Long amountMax;

    FilterBuilder(UUID ownerId) {
      this.ownerId = ownerId;
    }

    FilterBuilder withCategoryId(UUID v) { this.categoryId = v; return this; }
    FilterBuilder withStart(LocalDate v) { this.start = v; return this; }
    FilterBuilder withEnd(LocalDate v) { this.end = v; return this; }
    FilterBuilder withType(TransactionType v) { this.type = v; return this; }
    FilterBuilder withQuery(String v) { this.query = v; return this; }
    FilterBuilder withAmountMin(Long v) { this.amountMin = v; return this; }
    FilterBuilder withAmountMax(Long v) { this.amountMax = v; return this; }

    TransactionFilter build() {
      return new TransactionFilter(ownerId, categoryId, start, end, type, query, amountMin, amountMax);
    }
  }
}
