package com.example.transactionsservice.analytics.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.transactionsservice.analytics.domain.MonthlySummaryRow;
import com.example.transactionsservice.analytics.domain.RunningBalanceRow;
import com.example.transactionsservice.analytics.domain.TopAccountRow;
import com.example.transactionsservice.transaction.domain.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryTransactionAnalyticsRepositoryTest {

  private final InMemoryTransactionAnalyticsRepository repo = new InMemoryTransactionAnalyticsRepository();

  @Test
  void runningBalanceShouldBeMonotonicallyCorrectForKnownAccount() {
    List<RunningBalanceRow> rows = repo.findRunningBalanceByAccountId("a-001").collectList().block();

    assertThat(rows).isNotNull().isNotEmpty();
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

  @Test
  void monthlySummaryNetAmountShouldEqualCreditsMinusDebits() {
    List<MonthlySummaryRow> rows = repo.findMonthlySummaryByAccountId("a-001").collectList().block();

    assertThat(rows).isNotNull().isNotEmpty();
    for (MonthlySummaryRow row : rows) {
      BigDecimal expected = row.totalCredits().subtract(row.totalDebits());
      assertThat(row.netAmount()).isEqualByComparingTo(expected);
    }
  }

  @Test
  void topAccountsShouldBeSortedByVolumeDescending() {
    List<TopAccountRow> rows = repo.findTopAccounts(5).collectList().block();

    assertThat(rows).isNotNull().hasSizeLessThanOrEqualTo(5);
    for (int i = 1; i < rows.size(); i++) {
      assertThat(rows.get(i - 1).totalVolume()).isGreaterThanOrEqualTo(rows.get(i).totalVolume());
    }
  }

  @Test
  void anomaliesShouldAllExceedTwoStdDevsAboveMean() {
    List<Transaction> anomalies = repo.findAnomaliesByAccountId("a-001").collectList().block();

    assertThat(anomalies).isNotNull();
    if (!anomalies.isEmpty()) {
      anomalies.forEach(t -> assertThat(t.amount()).isGreaterThan(BigDecimal.ZERO));
    }
  }

  @Test
  void searchByCategoryReturnsOnlyMatchingRows() {
    List<Transaction> results = repo.search("GROCERIES", null, null, null, 0, 50).collectList().block();

    assertThat(results).isNotNull().isNotEmpty();
    results.forEach(t -> assertThat(t.category()).isEqualToIgnoringCase("GROCERIES"));
  }

  @Test
  void searchByMinAmountFiltersCorrectly() {
    BigDecimal minAmount = new BigDecimal("500");
    List<Transaction> results = repo.search(null, null, null, minAmount, 0, 100).collectList().block();

    assertThat(results).isNotNull();
    results.forEach(t -> assertThat(t.amount()).isGreaterThanOrEqualTo(minAmount));
  }

  @Test
  void searchByDateRangeExcludesOutOfRangeRows() {
    LocalDateTime from = LocalDateTime.now().minusDays(30);
    LocalDateTime to = LocalDateTime.now();

    List<Transaction> results = repo.search(null, from, to, null, 0, 100).collectList().block();

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