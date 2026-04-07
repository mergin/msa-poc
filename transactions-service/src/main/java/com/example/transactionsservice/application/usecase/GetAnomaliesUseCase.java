package com.example.transactionsservice.application.usecase;

import com.example.transactionsservice.domain.model.Transaction;
import com.example.transactionsservice.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Returns transactions that are statistical outliers for the given account —
 * those whose amount exceeds the per-account mean by more than two standard
 * deviations:
 *
 * <pre>
 * SELECT t.*
 * FROM transactions t
 * JOIN (
 *   SELECT account_id, AVG(amount) AS mean, STDDEV(amount) AS stddev
 *   FROM transactions WHERE account_id = :accountId GROUP BY account_id
 * ) stats ON t.account_id = stats.account_id
 * WHERE t.account_id = :accountId
 *   AND t.amount > (stats.mean + 2 * stats.stddev)
 * ORDER BY t.amount DESC
 * </pre>
 */
@Service
public class GetAnomaliesUseCase {

  private final TransactionRepository transactionRepository;

  public GetAnomaliesUseCase(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Flux<Transaction> execute(String accountId) {
    return transactionRepository.findAnomaliesByAccountId(accountId);
  }
}
