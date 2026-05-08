package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.*;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType.EXPENSE;
import static com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType.INCOME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TransactionSummaryIntegrationTest extends BaseMvcIntegrationTest {

  private static final String EUR = "EUR";
  private static final String USD = "USD";
  private static final String GBP = "GBP";
  private static final String MARCH = "2026-03";

  private String userId;
  private String otherUserId;

  @BeforeEach
  void setUp() {
    userId = createTestUser();
    otherUserId = createTestUser();
  }

  // ── helpers ────────────────────────────────────────────────────────────────

  private UUID createCategory(String ownerId, String name) throws Exception {
    var result = mvc.post().uri("/v1/categories")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new CategoryWrite().name(name)))
        .exchange();
    return parseBody(result, Category.class).getId();
  }

  private void createTransaction(
      String ownerId, UUID categoryId, long amount, TransactionType type, String currency, LocalDate date
  ) {
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

  private MonthlySummary getSummary(String ownerId) throws Exception {
    var result = mvc.get().uri("/v1/transactions/summary?month={m}&currency={c}", TransactionSummaryIntegrationTest.MARCH, TransactionSummaryIntegrationTest.EUR)
        .with(jwtForUser(ownerId))
        .exchange();
    assertThat(result).hasStatus(HttpStatus.OK);
    return parseBody(result, MonthlySummary.class);
  }

  private List<MonthlySummary> getTrend(String ownerId, String from) throws Exception {
    var result = mvc.get().uri("/v1/transactions/summary/trend?from={f}&to={t}&currency={c}", from, TransactionSummaryIntegrationTest.MARCH, TransactionSummaryIntegrationTest.EUR)
        .with(jwtForUser(ownerId))
        .exchange();
    assertThat(result).hasStatus(HttpStatus.OK);
    return objectMapper.readValue(
        result.getResponse().getContentAsString(),
        new TypeReference<>() {});
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class EmptyMonth {

    @Test
    void should_ReturnZeros_When_NoTransactions() throws Exception {
      var summary = getSummary(userId);

      assertThat(summary)
          .returns(MARCH, MonthlySummary::getMonth)
          .returns(EUR, MonthlySummary::getCurrency)
          .returns(0L, MonthlySummary::getIncome)
          .returns(0L, MonthlySummary::getExpense)
          .returns(0L, MonthlySummary::getBalance)
          .returns(0, MonthlySummary::getIncomeCount)
          .returns(0, MonthlySummary::getExpenseCount)
          .returns(0, MonthlySummary::getExcludedTransactionCount);
    }
  }

  @Nested
  class IncomeVsExpense {

    @Test
    void should_SumIncomeAndExpenseSeparately_AndComputeBalance() throws Exception {
      var catId = createCategory(userId, "Misc");
      createTransaction(userId, catId, 5000L, INCOME, EUR, LocalDate.of(2026, 3, 5));
      createTransaction(userId, catId, 2500L, INCOME, EUR, LocalDate.of(2026, 3, 12));
      createTransaction(userId, catId, 1000L, EXPENSE, EUR, LocalDate.of(2026, 3, 15));

      var summary = getSummary(userId);

      assertThat(summary)
          .returns(7500L, MonthlySummary::getIncome)
          .returns(1000L, MonthlySummary::getExpense)
          .returns(6500L, MonthlySummary::getBalance)
          .returns(2, MonthlySummary::getIncomeCount)
          .returns(1, MonthlySummary::getExpenseCount)
          .returns(0, MonthlySummary::getExcludedTransactionCount);
    }
  }

  @Nested
  class MixedCurrencies {

    @Test
    void should_SumMatchingCurrency_And_CountOthersAsExcluded() throws Exception {
      var catId = createCategory(userId, "Travel");
      createTransaction(userId, catId, 2000L, EXPENSE, EUR, LocalDate.of(2026, 3, 10));
      createTransaction(userId, catId, 4000L, INCOME, EUR, LocalDate.of(2026, 3, 11));
      createTransaction(userId, catId, 3000L, EXPENSE, USD, LocalDate.of(2026, 3, 10));
      createTransaction(userId, catId, 500L, INCOME, GBP, LocalDate.of(2026, 3, 11));

      var summary = getSummary(userId);

      assertThat(summary)
          .returns(4000L, MonthlySummary::getIncome)
          .returns(2000L, MonthlySummary::getExpense)
          .returns(2000L, MonthlySummary::getBalance)
          .returns(1, MonthlySummary::getIncomeCount)
          .returns(1, MonthlySummary::getExpenseCount)
          .returns(2, MonthlySummary::getExcludedTransactionCount);
    }
  }

  @Nested
  class BoundaryDates {

    @Test
    void should_IncludeBoundaryTransactions_When_OnFirstAndLastDayOfMonth() throws Exception {
      var catId = createCategory(userId, "Bills");
      createTransaction(userId, catId, 100L, EXPENSE, EUR, LocalDate.of(2026, 3, 1));
      createTransaction(userId, catId, 200L, EXPENSE, EUR, LocalDate.of(2026, 3, 31));
      createTransaction(userId, catId, 999L, EXPENSE, EUR, LocalDate.of(2026, 2, 28));
      createTransaction(userId, catId, 999L, EXPENSE, EUR, LocalDate.of(2026, 4, 1));

      var summary = getSummary(userId);

      assertThat(summary)
          .returns(300L, MonthlySummary::getExpense)
          .returns(2, MonthlySummary::getExpenseCount);
    }
  }

  @Nested
  class OwnerIsolation {

    @Test
    void should_NotCountOtherUsersTransactions() throws Exception {
      var myCat = createCategory(userId, "Mine");
      var otherCat = createCategory(otherUserId, "Other");
      createTransaction(userId, myCat, 1000L, EXPENSE, EUR, LocalDate.of(2026, 3, 10));
      createTransaction(otherUserId, otherCat, 9999L, EXPENSE, EUR, LocalDate.of(2026, 3, 10));
      createTransaction(otherUserId, otherCat, 8888L, INCOME, EUR, LocalDate.of(2026, 3, 10));

      var summary = getSummary(userId);

      assertThat(summary)
          .returns(1000L, MonthlySummary::getExpense)
          .returns(0L, MonthlySummary::getIncome)
          .returns(1, MonthlySummary::getExpenseCount)
          .returns(0, MonthlySummary::getIncomeCount);
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

  @Nested
  class Trend {

    @Test
    void should_ReturnOneBucketPerMonth_OldestFirst() throws Exception {
      var catId = createCategory(userId, "Misc");
      createTransaction(userId, catId, 1000L, EXPENSE, EUR, LocalDate.of(2026, 1, 5));
      createTransaction(userId, catId, 2000L, INCOME, EUR, LocalDate.of(2026, 2, 12));
      createTransaction(userId, catId, 500L, EXPENSE, EUR, LocalDate.of(2026, 3, 20));

      var trend = getTrend(userId, "2026-01");

      assertThat(trend)
          .extracting(MonthlySummary::getMonth, MonthlySummary::getIncome, MonthlySummary::getExpense)
          .containsExactly(
              tuple("2026-01", 0L, 1000L),
              tuple("2026-02", 2000L, 0L),
              tuple(MARCH, 0L, 500L));
    }

    @Test
    void should_ZeroFillEmptyMonths() throws Exception {
      var catId = createCategory(userId, "Misc");
      // Only Jan and Mar have transactions; Feb is empty.
      createTransaction(userId, catId, 1000L, EXPENSE, EUR, LocalDate.of(2026, 1, 5));
      createTransaction(userId, catId, 500L, EXPENSE, EUR, LocalDate.of(2026, 3, 20));

      var trend = getTrend(userId, "2026-01");

      assertThat(trend).hasSize(3);
      assertThat(trend.get(1))
          .returns("2026-02", MonthlySummary::getMonth)
          .returns(0L, MonthlySummary::getIncome)
          .returns(0L, MonthlySummary::getExpense)
          .returns(0L, MonthlySummary::getBalance)
          .returns(0, MonthlySummary::getIncomeCount)
          .returns(0, MonthlySummary::getExpenseCount)
          .returns(0, MonthlySummary::getExcludedTransactionCount)
          .returns(EUR, MonthlySummary::getCurrency);
    }

    @Test
    void should_ReturnSingleBucket_When_FromEqualsTo() throws Exception {
      var catId = createCategory(userId, "Misc");
      createTransaction(userId, catId, 7500L, INCOME, EUR, LocalDate.of(2026, 3, 5));

      var trend = getTrend(userId, MARCH);

      assertThat(trend)
          .singleElement()
          .returns(MARCH, MonthlySummary::getMonth)
          .returns(7500L, MonthlySummary::getIncome);
    }

    @Test
    void should_NotIncludeOtherUsersTransactions() throws Exception {
      var myCat = createCategory(userId, "Mine");
      var otherCat = createCategory(otherUserId, "Other");
      createTransaction(userId, myCat, 1000L, EXPENSE, EUR, LocalDate.of(2026, 2, 10));
      createTransaction(otherUserId, otherCat, 9999L, EXPENSE, EUR, LocalDate.of(2026, 2, 10));

      var trend = getTrend(userId, "2026-01");

      assertThat(trend)
          .extracting(MonthlySummary::getMonth, MonthlySummary::getExpense)
          .containsExactly(
              tuple("2026-01", 0L),
              tuple("2026-02", 1000L),
              tuple(MARCH, 0L));
    }

    @Test
    void should_Return400_When_FromAfterTo() {
      var result = mvc.get().uri("/v1/transactions/summary/trend?from=2026-05&to=2026-03&currency=EUR")
          .with(jwtForUser(userId))
          .exchange();
      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_RangeExceedsCap() {
      // 25 months (Jan 2024 → Jan 2026 inclusive) > 24-month cap.
      var result = mvc.get().uri("/v1/transactions/summary/trend?from=2024-01&to=2026-01&currency=EUR")
          .with(jwtForUser(userId))
          .exchange();
      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_FromMalformed() {
      var result = mvc.get().uri("/v1/transactions/summary/trend?from=2026-13&to=2026-05&currency=EUR")
          .with(jwtForUser(userId))
          .exchange();
      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return400_When_FromMissing() {
      var result = mvc.get().uri("/v1/transactions/summary/trend?to=2026-05&currency=EUR")
          .with(jwtForUser(userId))
          .exchange();
      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.get().uri("/v1/transactions/summary/trend?from=2026-01&to=2026-03&currency=EUR").exchange();
      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
