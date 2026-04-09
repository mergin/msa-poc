package com.example.transactionsservice.analytics.application;

import com.example.transactionsservice.analytics.domain.MonthlySummaryRow;
import com.example.transactionsservice.analytics.domain.TransactionAnalyticsRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class GetMonthlySummaryUseCase {

  private final TransactionAnalyticsRepository transactionAnalyticsRepository;

  public GetMonthlySummaryUseCase(TransactionAnalyticsRepository transactionAnalyticsRepository) {
    this.transactionAnalyticsRepository = transactionAnalyticsRepository;
  }

  public Flux<MonthlySummaryRow> execute(String accountId) {
    return transactionAnalyticsRepository.findMonthlySummaryByAccountId(accountId);
  }
}