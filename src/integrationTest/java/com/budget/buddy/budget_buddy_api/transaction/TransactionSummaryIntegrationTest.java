package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.MonthlySummary;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class TransactionSummaryIntegrationTest extends BaseMvcIntegrationTest {

  private String userId;
  private String otherUserId;

  // ── helpers ────────────────────────────────────────────────────────────────

  private UUID createCategory(String ownerId, String name) throws Exception {
    var body = new CategoryWrite().name(name).monthlyBudget(null);
    var result = mvc.post().uri("/v1/categories")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(body))
        .exchange();
    return parseBody(result, com.budget.buddy.budget_buddy_contracts.generated.model.Category.class).getId();
  }

  private void createTransaction(
      String ownerId, UUID categoryId, long amount, TransactionType type, String currency, LocalDate date
  ) throws Exception {
    var body = new TransactionWrite()
        .categoryId(categoryId)
        .amount(amount)
        .type(type)
        .currency(currency)
        .date(date);
    mvc.post().uri("/v1/transactions")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(body))
        .exchange();
  }

  private MonthlySummary getSummary(String ownerId, String month, String currency) throws Exception {
    var result = mvc.get().uri("/v1/transactions/summary?month={m}&currency={c}", month, currency)
        .with(jwtForUser(ownerId))
        .exchange();
    assertThat(result).hasStatus(HttpStatus.OK);
    return parseBody(result, MonthlySummary.class);
  }

  @BeforeEach
  void setUp() {
    userId = createTestUser();
    otherUserId = createTestUser();
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class EmptyMonth {

    @Test
    void should_ReturnZeros_When_NoTransactions() throws Exception {
      var summary = getSummary(userId, "2026-03", "EUR");

      assertThat(summary.getMonth()).isEqualTo("2026-03");
      assertThat(summary.getCurrency()).isEqualTo("EUR");
      assertThat(summary.getIncome()).isEqualTo(0L);
      assertThat(summary.getExpense()).isEqualTo(0L);
      assertThat(summary.getBalance()).isEqualTo(0L);
      assertThat(summary.getIncomeCount()).isEqualTo(0);
      assertThat(summary.getExpenseCount()).isEqualTo(0);
      assertThat(summary.getExcludedTransactionCount()).isEqualTo(0);
    }
  }

  @Nested
  class IncomeVsExpense {

    @Test
    void should_SumIncomeAndExpenseSeparately_AndComputeBalance() throws Exception {
      var catId = createCategory(userId, "Misc");
      createTransaction(userId, catId, 5000L, TransactionType.INCOME, "EUR", LocalDate.of(2026, 3, 5));
      createTransaction(userId, catId, 2500L, TransactionType.INCOME, "EUR", LocalDate.of(2026, 3, 12));
      createTransaction(userId, catId, 1000L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, 3, 15));

      var summary = getSummary(userId, "2026-03", "EUR");

      assertThat(summary.getIncome()).isEqualTo(7500L);
      assertThat(summary.getExpense()).isEqualTo(1000L);
      assertThat(summary.getBalance()).isEqualTo(6500L);
      assertThat(summary.getIncomeCount()).isEqualTo(2);
      assertThat(summary.getExpenseCount()).isEqualTo(1);
      assertThat(summary.getExcludedTransactionCount()).isEqualTo(0);
    }
  }

  @Nested
  class MixedCurrencies {

    @Test
    void should_SumMatchingCurrency_And_CountOthersAsExcluded() throws Exception {
      var catId = createCategory(userId, "Travel");
      createTransaction(userId, catId, 2000L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, 3, 10));
      createTransaction(userId, catId, 4000L, TransactionType.INCOME, "EUR", LocalDate.of(2026, 3, 11));
      createTransaction(userId, catId, 3000L, TransactionType.EXPENSE, "USD", LocalDate.of(2026, 3, 10));
      createTransaction(userId, catId, 500L, TransactionType.INCOME, "GBP", LocalDate.of(2026, 3, 11));

      var summary = getSummary(userId, "2026-03", "EUR");

      assertThat(summary.getIncome()).isEqualTo(4000L);
      assertThat(summary.getExpense()).isEqualTo(2000L);
      assertThat(summary.getBalance()).isEqualTo(2000L);
      assertThat(summary.getIncomeCount()).isEqualTo(1);
      assertThat(summary.getExpenseCount()).isEqualTo(1);
      assertThat(summary.getExcludedTransactionCount()).isEqualTo(2);
    }
  }

  @Nested
  class BoundaryDates {

    @Test
    void should_IncludeBoundaryTransactions_When_OnFirstAndLastDayOfMonth() throws Exception {
      var catId = createCategory(userId, "Bills");
      createTransaction(userId, catId, 100L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, 3, 1));
      createTransaction(userId, catId, 200L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, 3, 31));
      createTransaction(userId, catId, 999L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, 2, 28));
      createTransaction(userId, catId, 999L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, 4, 1));

      var summary = getSummary(userId, "2026-03", "EUR");

      assertThat(summary.getExpense()).isEqualTo(300L);
      assertThat(summary.getExpenseCount()).isEqualTo(2);
    }
  }

  @Nested
  class OwnerIsolation {

    @Test
    void should_NotCountOtherUsersTransactions() throws Exception {
      var myCat = createCategory(userId, "Mine");
      var otherCat = createCategory(otherUserId, "Other");
      createTransaction(userId, myCat, 1000L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, 3, 10));
      createTransaction(otherUserId, otherCat, 9999L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, 3, 10));
      createTransaction(otherUserId, otherCat, 8888L, TransactionType.INCOME, "EUR", LocalDate.of(2026, 3, 10));

      var summary = getSummary(userId, "2026-03", "EUR");

      assertThat(summary.getExpense()).isEqualTo(1000L);
      assertThat(summary.getIncome()).isEqualTo(0L);
      assertThat(summary.getExpenseCount()).isEqualTo(1);
      assertThat(summary.getIncomeCount()).isEqualTo(0);
    }
  }

  @Nested
  class Validation {

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.get().uri("/v1/transactions/summary?month=2026-03&currency=EUR").exchange();
      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_Return400_When_MonthFormatInvalid() {
      var result = mvc.get().uri("/v1/transactions/summary?month=2026-13&currency=EUR")
          .with(jwtForUser(userId))
          .exchange();
      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_CurrencyTooLong() {
      var result = mvc.get().uri("/v1/transactions/summary?month=2026-03&currency=EURO")
          .with(jwtForUser(userId))
          .exchange();
      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_MonthMissing() {
      var result = mvc.get().uri("/v1/transactions/summary?currency=EUR")
          .with(jwtForUser(userId))
          .exchange();
      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_CurrencyMissing() {
      var result = mvc.get().uri("/v1/transactions/summary?month=2026-03")
          .with(jwtForUser(userId))
          .exchange();
      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

}
