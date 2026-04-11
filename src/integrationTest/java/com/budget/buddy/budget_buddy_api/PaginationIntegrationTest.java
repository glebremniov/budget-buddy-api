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
  void should_ReturnCorrectPage_When_OffsetIsSkipCount_ForTransactions() throws Exception {
    // Given: 5 transactions, sorted by date DESC: T5, T4, T3, T2, T1
    // We want to skip 2 (T5, T4) and take 2 (T3, T2)
    int limit = 2;
    int offset = 2;

    // When
    var result = mvc.get().uri("/v1/transactions?limit={limit}&offset={offset}", limit, offset)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange();

    // Then
    assertThat(result).hasStatusOk();
    var page = parseBody(result, PaginatedTransactions.class);
    
    assertThat(page.getItems()).hasSize(2);
    assertThat(page.getItems().get(0).getAmount()).isEqualTo(300L);
    assertThat(page.getItems().get(1).getAmount()).isEqualTo(200L);
    
    assertThat(page.getMeta().getOffset()).isEqualTo(2);
    assertThat(page.getMeta().getLimit()).isEqualTo(2);
    assertThat(page.getMeta().getTotal()).isEqualTo(5L);
  }

  @Test
  void should_ReturnCorrectPage_When_OffsetIsSkipCount_ForCategories() throws Exception {
    // Create additional 4 categories (total 5)
    createCategory("Category 2");
    createCategory("Category 3");
    createCategory("Category 4");
    createCategory("Category 5");

    int limit = 2;
    int offset = 2;

    // When
    var result = mvc.get().uri("/v1/categories?limit={limit}&offset={offset}", limit, offset)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange();

    // Then
    assertThat(result).hasStatusOk();
    var page = parseBody(result, PaginatedCategories.class);
    
    assertThat(page.getItems()).hasSize(2);
    assertThat(page.getMeta().getOffset()).isEqualTo(2);
    assertThat(page.getMeta().getLimit()).isEqualTo(2);
    assertThat(page.getMeta().getTotal()).isEqualTo(5L);
  }
}
