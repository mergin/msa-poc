package com.example.transactionsservice.analytics.application;

import com.example.transactionsservice.analytics.domain.RunningBalanceRow;
import com.example.transactionsservice.analytics.domain.TransactionAnalyticsRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class GetRunningBalanceUseCase {

  private final TransactionAnalyticsRepository transactionAnalyticsRepository;

  public GetRunningBalanceUseCase(TransactionAnalyticsRepository transactionAnalyticsRepository) {
    this.transactionAnalyticsRepository = transactionAnalyticsRepository;
  }

  public Flux<RunningBalanceRow> execute(String accountId) {
    return transactionAnalyticsRepository.findRunningBalanceByAccountId(accountId);
  }
}