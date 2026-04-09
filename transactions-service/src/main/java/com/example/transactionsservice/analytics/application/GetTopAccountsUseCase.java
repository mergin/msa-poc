package com.example.transactionsservice.analytics.application;

import com.example.transactionsservice.analytics.domain.TopAccountRow;
import com.example.transactionsservice.analytics.domain.TransactionAnalyticsRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class GetTopAccountsUseCase {

  private static final int MAX_LIMIT = 50;

  private final TransactionAnalyticsRepository transactionAnalyticsRepository;

  public GetTopAccountsUseCase(TransactionAnalyticsRepository transactionAnalyticsRepository) {
    this.transactionAnalyticsRepository = transactionAnalyticsRepository;
  }

  public Flux<TopAccountRow> execute(int limit) {
    return transactionAnalyticsRepository.findTopAccounts(Math.min(limit, MAX_LIMIT));
  }
}