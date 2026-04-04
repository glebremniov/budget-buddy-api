package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionUpdate;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class TransactionIntegrationTest extends BaseMvcIntegrationTest {

  private String userToken;
  private String otherUserToken;
  private UUID userCategoryId;
  private UUID otherUserCategoryId;

  private UUID createCategory(String token, String name) throws Exception {
    var result = mvc.post().uri("/v1/categories")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new CategoryWrite().name(name)))
        .exchange();

    return parseBody(result, Category.class).getId();
  }

  private Transaction createTransaction(String token, UUID categoryId) throws Exception {
    return createTransaction(token, categoryId, null);
  }

  private Transaction createTransaction(String token, UUID categoryId, String description) throws Exception {
    var body = new TransactionWrite()
        .categoryId(categoryId)
        .amount(1000L)
        .type(TransactionWrite.TypeEnum.EXPENSE)
        .currency("EUR")
        .date(LocalDate.of(2026, 3, 1))
        .description(description);

    var result = mvc.post().uri("/v1/transactions")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(body))
        .exchange();

    return parseBody(result, Transaction.class);
  }

  @BeforeEach
  void setUp() throws Exception {
    userToken = registerAndLogin("txuser", "password123");
    otherUserToken = registerAndLogin("othertxuser", "password123");
    userCategoryId = createCategory(userToken, "Food");
    otherUserCategoryId = createCategory(otherUserToken, "Other Food");
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class Create {

    @Test
    void should_CreateTransaction_When_ValidRequest() throws Exception {
      var transaction = createTransaction(userToken, userCategoryId);

      assertThat(transaction.getId()).isNotNull();
      assertThat(transaction.getAmount()).isEqualTo(1000);
      assertThat(transaction.getCurrency()).isEqualTo("EUR");
      assertThat(transaction.getType()).isEqualTo(Transaction.TypeEnum.EXPENSE);
    }

    @Test
    void should_ReturnLocationHeader_When_Created() {
      var result = mvc.post().uri("/v1/transactions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(500L)
              .type(TransactionWrite.TypeEnum.INCOME)
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
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(otherUserCategoryId)
              .amount(1000L)
              .type(TransactionWrite.TypeEnum.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_CategoryDoesNotExist() {
      var result = mvc.post().uri("/v1/transactions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(UUID.randomUUID())
              .amount(1000L)
              .type(TransactionWrite.TypeEnum.EXPENSE)
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
              .type(TransactionWrite.TypeEnum.EXPENSE)
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
      var created = createTransaction(userToken, userCategoryId);

      var result = mvc.get().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var transaction = parseBody(result, Transaction.class);
      assertThat(transaction.getId()).isEqualTo(created.getId());
      assertThat(transaction.getAmount()).isEqualTo(1000);
    }

    @Test
    void should_Return404_When_TransactionBelongsToOtherUser() throws Exception {
      var created = createTransaction(otherUserToken, otherUserCategoryId);

      var result = mvc.get().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_TransactionNotFound() {
      var result = mvc.get().uri("/v1/transactions/{id}", UUID.randomUUID())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createTransaction(userToken, userCategoryId);

      var result = mvc.get().uri("/v1/transactions/{id}", created.getId())
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Replace {

    @Test
    void should_ReplaceTransaction_When_Owner() throws Exception {
      var created = createTransaction(userToken, userCategoryId, "original");

      var replaceBody = new TransactionWrite()
          .categoryId(userCategoryId)
          .amount(5000L)
          .type(TransactionWrite.TypeEnum.INCOME)
          .currency("USD")
          .date(LocalDate.of(2026, 6, 1));

      var result = mvc.put().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(replaceBody))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var replaced = parseBody(result, Transaction.class);
      assertThat(replaced)
          .returns(created.getId(), Transaction::getId)
          .returns(5000L, Transaction::getAmount)
          .returns("USD", Transaction::getCurrency)
          .returns(Transaction.TypeEnum.INCOME, Transaction::getType);
      assertThat(replaced.getDescription())
          .as("Description should be cleared when not provided in PUT body")
          .isNull();
    }

    @Test
    void should_Return400_When_ReplaceWithOtherUserCategory() throws Exception {
      var created = createTransaction(userToken, userCategoryId);

      var result = mvc.put().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(otherUserCategoryId)
              .amount(1000L)
              .type(TransactionWrite.TypeEnum.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return404_When_TransactionBelongsToOtherUser() throws Exception {
      var created = createTransaction(otherUserToken, otherUserCategoryId);

      var result = mvc.put().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(1000L)
              .type(TransactionWrite.TypeEnum.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createTransaction(userToken, userCategoryId);

      var result = mvc.put().uri("/v1/transactions/{id}", created.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(1000L)
              .type(TransactionWrite.TypeEnum.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 3, 1))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Update {

    @Test
    void should_UpdateTransaction_When_Owner() throws Exception {
      var created = createTransaction(userToken, userCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().amount(2000)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Transaction.class);
      assertThat(updated.getAmount()).isEqualTo(2000);
    }

    @Test
    void should_Return400_When_UpdateWithOtherUserCategory() throws Exception {
      var created = createTransaction(userToken, userCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().categoryId(otherUserCategoryId)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_UpdateWithNonExistentCategory() throws Exception {
      var created = createTransaction(userToken, userCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().categoryId(UUID.randomUUID())))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return404_When_TransactionBelongsToOtherUser() throws Exception {
      var created = createTransaction(otherUserToken, otherUserCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().amount(9999)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createTransaction(userToken, userCategoryId);

      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionUpdate().amount(9999)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_ClearDescription_When_ExplicitNullInPatch() throws Exception {
      // Given
      var created = createTransaction(userToken, userCategoryId, "A description");

      // When — raw JSON to explicitly send null (distinct from omitting the field)
      var result = mvc.patch().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
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
      var created = createTransaction(userToken, userCategoryId);

      var deleteResult = mvc.delete().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(deleteResult).hasStatus(HttpStatus.NO_CONTENT);

      var getResult = mvc.get().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(getResult).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_TransactionBelongsToOtherUser() throws Exception {
      var created = createTransaction(otherUserToken, otherUserCategoryId);

      var result = mvc.delete().uri("/v1/transactions/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createTransaction(userToken, userCategoryId);

      var result = mvc.delete().uri("/v1/transactions/{id}", created.getId())
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class List {

    @Test
    void should_ReturnOnlyOwnTransactions() throws Exception {
      createTransaction(userToken, userCategoryId);
      createTransaction(userToken, userCategoryId);
      createTransaction(otherUserToken, otherUserCategoryId);

      var result = mvc.get().uri("/v1/transactions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedTransactions.class);
      assertThat(page.getItems()).hasSize(2);
    }

    @Test
    void should_FilterByCategory() throws Exception {
      var otherCategoryId = createCategory(userToken, "Transport");
      createTransaction(userToken, userCategoryId);
      createTransaction(userToken, otherCategoryId);

      var result = mvc.get().uri("/v1/transactions?categoryId={categoryId}", userCategoryId)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
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
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(100L)
              .type(TransactionWrite.TypeEnum.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 1, 1))))
          .exchange();

      mvc.post().uri("/v1/transactions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(userCategoryId)
              .amount(200L)
              .type(TransactionWrite.TypeEnum.EXPENSE)
              .currency("EUR")
              .date(LocalDate.of(2026, 6, 1))))
          .exchange();

      var result = mvc.get().uri("/v1/transactions?start=2026-01-01&end=2026-03-31")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
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
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
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
  }
}
