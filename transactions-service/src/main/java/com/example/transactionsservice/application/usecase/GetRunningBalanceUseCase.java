package com.example.transactionsservice.application.usecase;

import com.example.transactionsservice.domain.model.projection.RunningBalanceRow;
import com.example.transactionsservice.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Returns every transaction for the given account together with a cumulative
 * running balance — computed with a SQL window function:
 *
 * <pre>
 * SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END)
 *   OVER (PARTITION BY account_id ORDER BY timestamp
 *         ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)
 * </pre>
 *
 * This demonstrates using raw {@code DatabaseClient} SQL for queries that are
 * not expressible through a plain repository {@code findAll()}.
 */
@Service
public class GetRunningBalanceUseCase {

  private final TransactionRepository transactionRepository;

  public GetRunningBalanceUseCase(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Flux<RunningBalanceRow> execute(String accountId) {
    return transactionRepository.findRunningBalanceByAccountId(accountId);
  }
}
