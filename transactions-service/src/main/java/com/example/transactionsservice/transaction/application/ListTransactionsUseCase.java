package com.example.transactionsservice.transaction.application;

import com.example.transactionsservice.transaction.domain.Transaction;
import com.example.transactionsservice.transaction.domain.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ListTransactionsUseCase {

  private static final int MAX_SIZE = 100;

  private final TransactionRepository transactionRepository;

  public ListTransactionsUseCase(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Flux<Transaction> execute(int page, int size) {
    return transactionRepository.findPage(page, Math.min(size, MAX_SIZE));
  }
}