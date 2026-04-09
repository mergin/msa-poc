package com.example.transactionsservice.analytics.application;

import com.example.transactionsservice.analytics.domain.TransactionAnalyticsRepository;
import com.example.transactionsservice.transaction.domain.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class SearchTransactionsUseCase {

  private static final int MAX_SIZE = 100;

  private final TransactionAnalyticsRepository transactionAnalyticsRepository;

  public SearchTransactionsUseCase(TransactionAnalyticsRepository transactionAnalyticsRepository) {
    this.transactionAnalyticsRepository = transactionAnalyticsRepository;
  }

  public Flux<Transaction> execute(
      String category,
      LocalDateTime from,
      LocalDateTime to,
      BigDecimal minAmount,
      int page,
      int size) {
    return transactionAnalyticsRepository.search(
        category, from, to, minAmount, page, Math.min(size, MAX_SIZE));
  }
}