package com.example.transactionsservice.application.usecase;

import com.example.transactionsservice.domain.model.projection.MonthlySummaryRow;
import com.example.transactionsservice.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Groups all transactions for the given account by calendar month and returns
 * aggregated totals:
 *
 * <pre>
 * SELECT DATE_TRUNC('month', timestamp) AS month,
 *        SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE 0 END) AS total_credits,
 *        SUM(CASE WHEN type = 'DEBIT'  THEN amount ELSE 0 END) AS total_debits,
 *        SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END) AS net_amount,
 *        COUNT(*) AS transaction_count
 * FROM transactions
 * WHERE account_id = :accountId
 * GROUP BY DATE_TRUNC('month', timestamp)
 * ORDER BY month
 * </pre>
 */
@Service
public class GetMonthlySummaryUseCase {

  private final TransactionRepository transactionRepository;

  public GetMonthlySummaryUseCase(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Flux<MonthlySummaryRow> execute(String accountId) {
    return transactionRepository.findMonthlySummaryByAccountId(accountId);
  }
}
