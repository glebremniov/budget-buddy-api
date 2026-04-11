package com.budget.buddy.budget_buddy_api;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedCategories;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class PaginationIntegrationTest extends BaseMvcIntegrationTest {

  private String token;
  private UUID categoryId;

  private Category createCategory(String name) throws Exception {
    var result = mvc.post().uri("/v1/categories")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new CategoryWrite().name(name)))
        .exchange();
    assertThat(result).hasStatus2xxSuccessful();
    return parseBody(result, Category.class);
  }

  @BeforeEach
  void setUp() throws Exception {
    token = registerAndLogin("pageuser_" + UUID.randomUUID(), "password123");
    categoryId = createCategory("Test Category").getId();

    // Create 5 transactions: T1 (100, Jan 1), T2 (200, Jan 2), T3 (300, Jan 3), T4 (400, Jan 4), T5 (500, Jan 5)
    // Sorted DESC: T5, T4, T3, T2, T1
    for (int i = 1; i <= 5; i++) {
      var result = mvc.post().uri("/v1/transactions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new TransactionWrite()
              .categoryId(categoryId)
              .amount(i * 100L)
              .type(TransactionWrite.TypeEnum.EXPENSE)
              .currency("USD")
              .date(LocalDate.of(2026, 1, i))
              .description("Transaction " + i)))
          .exchange();
      assertThat(result).hasStatus2xxSuccessful();
    }
  }

  @Test
  void should_ReturnSecondPage_When_PageIs1AndSizeIs2_ForTransactions() throws Exception {
    // Given: 5 transactions, sorted by date DESC: T5, T4, T3, T2, T1
    // page=1, size=2 skips the first 2 (T5, T4) and returns the next 2 (T3, T2)
    int page = 1;
    int size = 2;

    // When
    var result = mvc.get().uri("/v1/transactions?page={page}&size={size}", page, size)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange();

    // Then
    assertThat(result).hasStatusOk();
    var response = parseBody(result, PaginatedTransactions.class);

    assertThat(response.getItems()).hasSize(2);
    assertThat(response.getItems().get(0).getAmount()).isEqualTo(300L); // T3
    assertThat(response.getItems().get(1).getAmount()).isEqualTo(200L); // T2

    assertThat(response.getMeta().getPage()).isEqualTo(1);
    assertThat(response.getMeta().getSize()).isEqualTo(2);
    assertThat(response.getMeta().getTotal()).isEqualTo(5L);
  }

  @Test
  void should_ReturnPartialLastPage_When_TotalIsNotDivisibleBySize_ForTransactions() throws Exception {
    // Given: 5 transactions, sorted by date DESC: T5, T4, T3, T2, T1
    // page=2, size=2 skips the first 4 (T5, T4, T3, T2) and returns the remainder (T1)
    int page = 2;
    int size = 2;

    // When
    var result = mvc.get().uri("/v1/transactions?page={page}&size={size}", page, size)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange();

    // Then
    assertThat(result).hasStatusOk();
    var response = parseBody(result, PaginatedTransactions.class);

    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().get(0).getAmount()).isEqualTo(100L); // T1

    assertThat(response.getMeta().getPage()).isEqualTo(2);
    assertThat(response.getMeta().getSize()).isEqualTo(2);
    assertThat(response.getMeta().getTotal()).isEqualTo(5L);
  }

  @Test
  void should_ReturnSecondPage_When_PageIs1AndSizeIs2_ForCategories() throws Exception {
    // Create additional 4 categories (total 5)
    createCategory("Category 2");
    createCategory("Category 3");
    createCategory("Category 4");
    createCategory("Category 5");

    int page = 1;
    int size = 2;

    // When
    var result = mvc.get().uri("/v1/categories?page={page}&size={size}", page, size)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange();

    // Then
    assertThat(result).hasStatusOk();
    var response = parseBody(result, PaginatedCategories.class);

    assertThat(response.getItems()).hasSize(2);
    // Assuming default sort is by creation order (id)
    assertThat(response.getItems().get(0).getName()).isEqualTo("Category 3");
    assertThat(response.getItems().get(1).getName()).isEqualTo("Category 4");

    assertThat(response.getMeta().getPage()).isEqualTo(1);
    assertThat(response.getMeta().getSize()).isEqualTo(2);
    assertThat(response.getMeta().getTotal()).isEqualTo(5L);
  }
}
