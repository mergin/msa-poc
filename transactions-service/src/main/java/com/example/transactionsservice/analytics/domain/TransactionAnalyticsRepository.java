package com.example.transactionsservice.analytics.domain;

import com.example.transactionsservice.transaction.domain.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import reactor.core.publisher.Flux;

public interface TransactionAnalyticsRepository {

  Flux<RunningBalanceRow> findRunningBalanceByAccountId(String accountId);

  Flux<MonthlySummaryRow> findMonthlySummaryByAccountId(String accountId);

  Flux<TopAccountRow> findTopAccounts(int limit);

  Flux<Transaction> findAnomaliesByAccountId(String accountId);

  Flux<Transaction> search(
      String category,
      LocalDateTime from,
      LocalDateTime to,
      BigDecimal minAmount,
      int page,
      int size);
}