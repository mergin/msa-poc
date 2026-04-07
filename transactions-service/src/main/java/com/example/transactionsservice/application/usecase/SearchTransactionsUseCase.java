package com.example.transactionsservice.application.usecase;

import com.example.transactionsservice.domain.model.Transaction;
import com.example.transactionsservice.domain.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Multi-criteria search with all predicates optional.  The repository
 * implementation builds the WHERE clause dynamically so that only the
 * supplied filters are applied.
 *
 * <p>Supported filters:
 * <ul>
 *   <li>{@code category}   – exact match on the transaction category</li>
 *   <li>{@code from}       – lower bound (inclusive) on timestamp</li>
 *   <li>{@code to}         – upper bound (inclusive) on timestamp</li>
 *   <li>{@code minAmount}  – minimum transaction amount</li>
 * </ul>
 */
@Service
public class SearchTransactionsUseCase {

  private static final int MAX_SIZE = 100;

  private final TransactionRepository transactionRepository;

  public SearchTransactionsUseCase(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Flux<Transaction> execute(
      String category,
      LocalDateTime from,
      LocalDateTime to,
      BigDecimal minAmount,
      int page,
      int size) {
    return transactionRepository.search(
        category, from, to, minAmount, page, Math.min(size, MAX_SIZE));
  }
}
