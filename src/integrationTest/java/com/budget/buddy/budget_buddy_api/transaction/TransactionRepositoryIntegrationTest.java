package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseIntegrationTest;
import com.budget.buddy.budget_buddy_api.category.CategoryEntity;
import com.budget.buddy.budget_buddy_api.category.CategoryRepository;
import com.budget.buddy.budget_buddy_api.user.UserEntity;
import com.budget.buddy.budget_buddy_api.user.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@DisplayName("TransactionRepository Integration Tests")
class TransactionRepositoryIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private UserRepository userRepository;

  private UUID ownerId;
  private UUID categoryId;

  @BeforeEach
  void setUp() {
    var user = UserEntity.builder()
        .username("owner_" + UUID.randomUUID())
        .password("password")
        .enabled(true)
        .build();
    ownerId = userRepository.save(user).getId();

    var category = new CategoryEntity();
    category.setName("Food");
    category.setOwnerId(ownerId);
    categoryId = categoryRepository.save(category).getId();
  }

  @Test
  @DisplayName("should filter transactions by owner, date and category")
  void shouldFilterTransactions() {
    // Given
    var t1 = new TransactionEntity();
    t1.setAmount(100);
    t1.setCurrency("USD");
    t1.setType(TransactionType.EXPENSE);
    t1.setDate(LocalDate.now().minusDays(1));
    t1.setOwnerId(ownerId);
    t1.setCategoryId(categoryId);
    transactionRepository.save(t1);

    var t2 = new TransactionEntity();
    t2.setAmount(200);
    t2.setCurrency("USD");
    t2.setType(TransactionType.EXPENSE);
    t2.setDate(LocalDate.now());
    t2.setOwnerId(ownerId);
    t2.setCategoryId(categoryId);
    var expectedId = transactionRepository.save(t2).getId();

    // When
    var filter = new TransactionFilter(ownerId, categoryId, LocalDate.now(), null);
    var filtered = transactionRepository.findAllByFilter(filter, PageRequest.of(0, 10));

    // Then
    assertThat(filtered)
        .hasSize(1)
        .first()
        .returns(expectedId, TransactionEntity::getId);
  }

  @Test
  @DisplayName("should count transactions by filter")
  void shouldCountByFilter() {
    // Given
    var t1 = new TransactionEntity();
    t1.setAmount(100);
    t1.setCurrency("USD");
    t1.setType(TransactionType.EXPENSE);
    t1.setDate(LocalDate.now());
    t1.setOwnerId(ownerId);
    t1.setCategoryId(categoryId);
    transactionRepository.save(t1);

    // When
    var filter = new TransactionFilter(ownerId, categoryId, null, null);
    long count = transactionRepository.countByFilter(filter);

    // Then
    assertThat(count).isEqualTo(1);
  }

  @Test
  @DisplayName("should save and find transaction by ID and owner ID")
  void shouldSaveAndFindByIdAndOwnerId() {
    // Given
    var transaction = new TransactionEntity();
    transaction.setAmount(50);
    transaction.setCurrency("EUR");
    transaction.setType(TransactionType.EXPENSE);
    transaction.setDate(LocalDate.now());
    transaction.setOwnerId(ownerId);
    transaction.setCategoryId(categoryId);
    var saved = transactionRepository.save(transaction);

    // When
    var found = transactionRepository.findByIdAndOwnerId(saved.getId(), ownerId);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getAmount()).isEqualTo(50);
  }
}
