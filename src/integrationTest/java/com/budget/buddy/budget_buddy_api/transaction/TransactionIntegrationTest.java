package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.*;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionIntegrationTest extends BaseMvcIntegrationTest {

  private String userId;
  private String otherUserId;
  private UUID userCategoryId;
  private UUID otherUserCategoryId;

  private UUID createCategory(String ownerId, String name) throws Exception {
    var result = mvc.post().uri("/v1/categories")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new CategoryWrite().name(name)))
        .exchange();

    return parseBody(result, Category.class).getId();
  }

  private Transaction createTransaction(String ownerId, UUID categoryId) throws Exception {
    return createTransaction(ownerId, categoryId, null);
  }

  private Transaction createTransaction(String ownerId, UUID categoryId, String description) throws Exception {
    return createTransaction(ownerId, categoryId, description, 1000L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));
  }

  private Transaction createTransaction(
      String ownerId, UUID categoryId, String description, long amount, TransactionType type, LocalDate date
  ) throws Exception {
    var body = new TransactionWrite()
        .categoryId(categoryId)
        .amount(amount)
        .type(type)
        .currency("EUR")
        .date(date)
        .description(description);

    var result = mvc.post().uri("/v1/transactions")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(body))
        .exchange();

    return parseBody(result, Transaction.class);
  }

  @BeforeEach
  void setUp() throws Exception {
    userId = createTestUser();
    otherUserId = createTestUser();
    userCategoryId = createCategory(userId, "Food");
    otherUserCategoryId = createCategory(otherUserId, "Other Food");
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class Create {

    @Test
    void should_CreateTransaction_When_ValidRequest() throws Exception {
      var transaction = createTransaction(userId, userCategoryId);

      assertThat(transaction.getId()).isNotNull();
      assertThat(transaction.getAmount()).isEqualTo(1000);
      assertThat(transaction.getCurrency()).isEqualTo("EUR");
      assertThat(transaction.getType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void should_ReturnLocationHeader_When_Created() {
      var result = mvc.post().uri("/v1/transactions")
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(500L)
              .type(TransactionType.INCOME)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result)
          .hasStatus(HttpStatus.CREATED)
          .containsHeader("Location");
    }

    @Test
    void should_Return400_When_CategoryBelongsToOtherUser() {
      var result = mvc.post().uri("/v1/transactions")
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(otherUserCategoryId)
              .amount(1000L)
              .type(TransactionType.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_CategoryDoesNotExist() {
      var result = mvc.post().uri("/v1/transactions")
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(UUID.randomUUID())
              .amount(1000L)
              .type(TransactionType.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.post().uri("/v1/transactions")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(1000L)
              .type(TransactionType.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Read {

    @Test
    void should_ReturnTransaction_When_Owner() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var result = mvc.get().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var transaction = parseBody(result, Transaction.class);
      assertThat(transaction.getId()).isEqualTo(created.getId());
      assertThat(transaction.getAmount()).isEqualTo(1000);
    }

    @Test
    void should_Return404_When_TransactionBelongsToOtherUser() throws Exception {
      var created = createTransaction(otherUserId, otherUserCategoryId);

      var result = mvc.get().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_TransactionNotFound() {
      var result = mvc.get().uri("/v1/transactions/{id}", UUID.randomUUID())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var result = mvc.get().uri("/v1/transactions/{id}", created.getId())
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Replace {

    @Test
    void should_ReplaceTransaction_When_Owner() throws Exception {
      var created = createTransaction(userId, userCategoryId, "original");

      var replaceBody = new TransactionWrite()
          .categoryId(userCategoryId)
          .amount(5000L)
          .type(TransactionType.INCOME)
          .currency("USD")
          .date(LocalDate.of(2026, 6, 1));

      var result = mvc.put().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(replaceBody))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var replaced = parseBody(result, Transaction.class);
      assertThat(replaced)
          .returns(created.getId(), Transaction::getId)
          .returns(5000L, Transaction::getAmount)
          .returns("USD", Transaction::getCurrency)
          .returns(TransactionType.INCOME, Transaction::getType);
      assertThat(replaced.getDescription())
          .as("Description should be cleared when not provided in PUT body")
          .isNull();
    }

    @Test
    void should_Return400_When_ReplaceWithOtherUserCategory() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var result = mvc.put().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(otherUserCategoryId)
              .amount(1000L)
              .type(TransactionType.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return404_When_TransactionBelongsToOtherUser() throws Exception {
      var created = createTransaction(otherUserId, otherUserCategoryId);

      var result = mvc.put().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(1000L)
              .type(TransactionType.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var result = mvc.put().uri("/v1/transactions/{id}", created.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(1000L)
              .type(TransactionType.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Update {

    public static final Long AMOUNT = 2000L;

    @Test
    void should_UpdateTransaction_When_Owner() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().amount(AMOUNT)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Transaction.class);
      assertThat(updated)
          .returns(AMOUNT, Transaction::getAmount);
    }

    @Test
    void should_Return400_When_UpdateWithOtherUserCategory() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().categoryId(otherUserCategoryId)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_UpdateWithNonExistentCategory() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().categoryId(UUID.randomUUID())))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return404_When_TransactionBelongsToOtherUser() throws Exception {
      var created = createTransaction(otherUserId, otherUserCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().amount(AMOUNT)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().amount(AMOUNT)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_ClearDescription_When_ExplicitNullInPatch() throws Exception {
      // Given
      var created = createTransaction(userId, userCategoryId, "A description");

      // When — raw JSON to explicitly send null (distinct from omitting the field)
      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"description\": null}")
          .exchange();

      // Then
      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Transaction.class);
      assertThat(updated.getDescription())
          .as("Description should be cleared when PATCH sends explicit null")
          .isNull();
    }
  }

  @Nested
  class Delete {

    @Test
    void should_DeleteTransaction_When_Owner() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var deleteResult = mvc.delete().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(deleteResult).hasStatus(HttpStatus.NO_CONTENT);

      var getResult = mvc.get().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(getResult).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_TransactionBelongsToOtherUser() throws Exception {
      var created = createTransaction(otherUserId, otherUserCategoryId);

      var result = mvc.delete().uri("/v1/transactions/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createTransaction(userId, userCategoryId);

      var result = mvc.delete().uri("/v1/transactions/{id}", created.getId())
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class List {

    @Test
    void should_ReturnOnlyOwnTransactionsOrderedByDateDesc() throws Exception {
      var txn1 = createTransaction(userId, userCategoryId);
      var txn2 = createTransaction(userId, userCategoryId);
      createTransaction(otherUserId, otherUserCategoryId);

      var result = mvc.get().uri("/v1/transactions")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems())
          .hasSize(2)
          .extracting(Transaction::getId)
          .containsExactly(txn2.getId(), txn1.getId());
    }

    @Test
    void should_FilterByCategory() throws Exception {
      var otherCategoryId = createCategory(userId, "Transport");
      createTransaction(userId, userCategoryId);
      createTransaction(userId, otherCategoryId);

      var result = mvc.get().uri("/v1/transactions?categoryId={categoryId}", userCategoryId)
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems())
          .hasSize(1)
          .first()
          .returns(userCategoryId, Transaction::getCategoryId);
    }

    @Test
    void should_FilterByDateRange() throws Exception {
      mvc.post().uri("/v1/transactions")
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(100L)
              .type(TransactionType.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 1, 1))))
          .exchange();

      mvc.post().uri("/v1/transactions")
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(200L)
              .type(TransactionType.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 6, 1))))
          .exchange();

      var result = mvc.get().uri("/v1/transactions?start=2026-01-01&end=2026-03-31")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems())
          .hasSize(1)
          .first()
          .returns(100L, Transaction::getAmount);
    }

    @Test
    void should_ReturnEmptyList_When_NoTransactions() throws Exception {
      var result = mvc.get().uri("/v1/transactions")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems()).isEmpty();
      assertThat(page.getMeta().getTotal()).isZero();
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.get().uri("/v1/transactions")
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_FilterByQuery_AgainstDescription() throws Exception {
      var match = createTransaction(userId, userCategoryId, "Morning Coffee");
      createTransaction(userId, userCategoryId, "Lunch");

      var result = mvc.get().uri("/v1/transactions?query={q}", "coffee")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems())
          .singleElement()
          .returns(match.getId(), Transaction::getId);
    }

    @Test
    void should_FilterByQuery_AgainstCategoryName() throws Exception {
      var travelCategoryId = createCategory(userId, "Travel");
      var match = createTransaction(userId, travelCategoryId, "Hotel");
      createTransaction(userId, userCategoryId, "Groceries");

      var result = mvc.get().uri("/v1/transactions?query={q}", "TRAV")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems())
          .singleElement()
          .returns(match.getId(), Transaction::getId);
    }

    @Test
    void should_FilterByAmountRange_Inclusive() throws Exception {
      var low = createTransaction(userId, userCategoryId, "low", 100L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));
      var mid = createTransaction(userId, userCategoryId, "mid", 500L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));
      var high = createTransaction(userId, userCategoryId, "high", 1000L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));
      createTransaction(userId, userCategoryId, "tooLow", 99L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));
      createTransaction(userId, userCategoryId, "tooHigh", 1001L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));

      var result = mvc.get().uri("/v1/transactions?amountMin=100&amountMax=1000")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems())
          .extracting(Transaction::getId)
          .containsExactlyInAnyOrder(low.getId(), mid.getId(), high.getId());
    }

    @Test
    void should_FilterByAmount_Exact_When_MinEqualsMax() throws Exception {
      var exact = createTransaction(userId, userCategoryId, "exact", 250L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));
      createTransaction(userId, userCategoryId, "off-by-one", 251L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));

      var result = mvc.get().uri("/v1/transactions?amountMin=250&amountMax=250")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems())
          .singleElement()
          .returns(exact.getId(), Transaction::getId);
    }

    @Test
    void should_CombineQueryAndAmount_AndCategory() throws Exception {
      var travelCategoryId = createCategory(userId, "Travel");
      var match = createTransaction(userId, travelCategoryId, "Hotel in Paris", 500L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));
      createTransaction(userId, travelCategoryId, "Hotel in Paris", 50L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));
      createTransaction(userId, userCategoryId, "Hotel in Paris", 500L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));
      createTransaction(userId, travelCategoryId, "Souvenir", 500L, TransactionType.EXPENSE, LocalDate.of(2026, 3, 1));

      var result = mvc.get().uri(
              "/v1/transactions?query={q}&amountMin=100&amountMax=1000&categoryId={c}",
              "hotel", travelCategoryId)
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems())
          .singleElement()
          .returns(match.getId(), Transaction::getId);
    }

    @Test
    void should_Return400_When_AmountMinBelowOne() {
      var result = mvc.get().uri("/v1/transactions?amountMin=0")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_QueryExceedsMaxLength() {
      var tooLong = "x".repeat(256);

      var result = mvc.get().uri("/v1/transactions?query={q}", tooLong)
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }
  }
}
