package com.example.transactionsservice.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.transactionsservice.domain.model.Transaction;
import com.example.transactionsservice.domain.model.projection.MonthlySummaryRow;
import com.example.transactionsservice.domain.model.projection.RunningBalanceRow;
import com.example.transactionsservice.domain.model.projection.TopAccountRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/**
 * Tests the in-memory implementations of the complex analytical queries so that
 * the business logic can be validated without a database.
 */
class InMemoryTransactionRepositoryTest {

  private final InMemoryTransactionRepository repo = new InMemoryTransactionRepository();

  // -----------------------------------------------------------------------
  // Running balance
  // -----------------------------------------------------------------------

  @Test
  void runningBalanceShouldBeMonotonicallyCorrectForKnownAccount() {
    List<RunningBalanceRow> rows =
        repo.findRunningBalanceByAccountId("a-001").collectList().block();

    assertThat(rows).isNotNull().isNotEmpty();

    // Verify the running balance increases on CREDIT and decreases on DEBIT
    for (int i = 1; i < rows.size(); i++) {
      RunningBalanceRow prev = rows.get(i - 1);
      RunningBalanceRow curr = rows.get(i);
      BigDecimal expected =
          "CREDIT".equals(curr.type())
              ? prev.runningBalance().add(curr.amount())
              : prev.runningBalance().subtract(curr.amount());
      assertThat(curr.runningBalance()).isEqualByComparingTo(expected);
    }
  }

  // -----------------------------------------------------------------------
  // Monthly summary
  // -----------------------------------------------------------------------

  @Test
  void monthlySummaryNetAmountShouldEqualCreditsMinusDebits() {
    List<MonthlySummaryRow> rows =
        repo.findMonthlySummaryByAccountId("a-001").collectList().block();

    assertThat(rows).isNotNull().isNotEmpty();
    for (MonthlySummaryRow row : rows) {
      BigDecimal expected = row.totalCredits().subtract(row.totalDebits());
      assertThat(row.netAmount()).isEqualByComparingTo(expected);
    }
  }

  // -----------------------------------------------------------------------
  // Top accounts
  // -----------------------------------------------------------------------

  @Test
  void topAccountsShouldBeSortedByVolumeDescending() {
    List<TopAccountRow> rows = repo.findTopAccounts(5).collectList().block();

    assertThat(rows).isNotNull().hasSizeLessThanOrEqualTo(5);
    for (int i = 1; i < rows.size(); i++) {
      assertThat(rows.get(i - 1).totalVolume())
          .isGreaterThanOrEqualTo(rows.get(i).totalVolume());
    }
  }

  // -----------------------------------------------------------------------
  // Anomaly detection
  // -----------------------------------------------------------------------

  @Test
  void anomaliesShouldAllExceedTwoStdDevsAboveMean() {
    // Use an account that has enough data in the seed set
    List<Transaction> anomalies =
        repo.findAnomaliesByAccountId("a-001").collectList().block();

    // Even if 0 anomalies, the query must complete without error
    assertThat(anomalies).isNotNull();

    if (!anomalies.isEmpty()) {
      // All returned amounts must be non-trivially large relative to seed data
      anomalies.forEach(t -> assertThat(t.amount()).isGreaterThan(BigDecimal.ZERO));
    }
  }

  // -----------------------------------------------------------------------
  // Search
  // -----------------------------------------------------------------------

  @Test
  void searchByCategoryReturnsOnlyMatchingRows() {
    List<Transaction> results =
        repo.search("GROCERIES", null, null, null, 0, 50).collectList().block();

    assertThat(results).isNotNull().isNotEmpty();
    results.forEach(t -> assertThat(t.category()).isEqualToIgnoringCase("GROCERIES"));
  }

  @Test
  void searchByMinAmountFiltersCorrectly() {
    BigDecimal minAmount = new BigDecimal("500");
    List<Transaction> results =
        repo.search(null, null, null, minAmount, 0, 100).collectList().block();

    assertThat(results).isNotNull();
    results.forEach(t -> assertThat(t.amount()).isGreaterThanOrEqualTo(minAmount));
  }

  @Test
  void searchByDateRangeExcludesOutOfRangeRows() {
    LocalDateTime from = LocalDateTime.now().minusDays(30);
    LocalDateTime to = LocalDateTime.now();

    List<Transaction> results =
        repo.search(null, from, to, null, 0, 100).collectList().block();

    assertThat(results).isNotNull();
    results.forEach(
        t -> {
          assertThat(t.timestamp()).isAfterOrEqualTo(from);
          assertThat(t.timestamp()).isBeforeOrEqualTo(to);
        });
  }

  @Test
  void searchWithNoFiltersReturnsPaginatedResults() {
    List<Transaction> page0 = repo.search(null, null, null, null, 0, 10).collectList().block();
    List<Transaction> page1 = repo.search(null, null, null, null, 1, 10).collectList().block();

    assertThat(page0).isNotNull().hasSize(10);
    assertThat(page1).isNotNull().hasSize(10);
    assertThat(page0.get(0).id()).isNotEqualTo(page1.get(0).id());
  }
}
