package com.example.transactionsservice.domain.repository;

import com.example.transactionsservice.domain.model.Transaction;
import com.example.transactionsservice.domain.model.projection.MonthlySummaryRow;
import com.example.transactionsservice.domain.model.projection.RunningBalanceRow;
import com.example.transactionsservice.domain.model.projection.TopAccountRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionRepository {

  Flux<Transaction> findPage(int page, int size);

  Mono<Transaction> findById(String id);

  /**
   * Window-function query: running signed balance per transaction,
   * partitioned by account and ordered chronologically.
   */
  Flux<RunningBalanceRow> findRunningBalanceByAccountId(String accountId);

  /**
   * GROUP BY DATE_TRUNC('month', timestamp): monthly debit/credit/net aggregates
   * for a single account.
   */
  Flux<MonthlySummaryRow> findMonthlySummaryByAccountId(String accountId);

  /**
   * Ranking query: top N accounts by total transaction volume (SUM of amounts).
   */
  Flux<TopAccountRow> findTopAccounts(int limit);

  /**
   * Statistical anomaly detection: transactions whose amount exceeds
   * the per-account mean by more than two standard deviations.
   */
  Flux<Transaction> findAnomaliesByAccountId(String accountId);

  /**
   * Multi-filter search with optional predicates on category, date range,
   * and minimum amount. Paginated.
   */
  Flux<Transaction> search(
      String category,
      LocalDateTime from,
      LocalDateTime to,
      BigDecimal minAmount,
      int page,
      int size);
}
