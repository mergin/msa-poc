package com.example.transactionsservice.analytics.application;

import com.example.transactionsservice.analytics.domain.TransactionAnalyticsRepository;
import com.example.transactionsservice.transaction.domain.Transaction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class GetAnomaliesUseCase {

  private final TransactionAnalyticsRepository transactionAnalyticsRepository;

  public GetAnomaliesUseCase(TransactionAnalyticsRepository transactionAnalyticsRepository) {
    this.transactionAnalyticsRepository = transactionAnalyticsRepository;
  }

  public Flux<Transaction> execute(String accountId) {
    return transactionAnalyticsRepository.findAnomaliesByAccountId(accountId);
  }
}