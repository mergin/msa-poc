package com.example.transactionsservice.application.usecase;

import com.example.transactionsservice.domain.model.projection.TopAccountRow;
import com.example.transactionsservice.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Ranks accounts by total transaction volume using a GROUP BY + ORDER BY query.
 *
 * <pre>
 * SELECT account_id,
 *        SUM(amount) AS total_volume,
 *        COUNT(*)    AS transaction_count
 * FROM transactions
 * GROUP BY account_id
 * ORDER BY total_volume DESC
 * LIMIT :limit
 * </pre>
 */
@Service
public class GetTopAccountsUseCase {

  private static final int MAX_LIMIT = 50;

  private final TransactionRepository transactionRepository;

  public GetTopAccountsUseCase(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Flux<TopAccountRow> execute(int limit) {
    return transactionRepository.findTopAccounts(Math.min(limit, MAX_LIMIT));
  }
}
