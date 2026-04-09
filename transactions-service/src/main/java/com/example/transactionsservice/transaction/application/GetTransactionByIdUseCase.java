package com.example.transactionsservice.transaction.application;

import com.example.transactionsservice.transaction.api.exception.ResourceNotFoundException;
import com.example.transactionsservice.transaction.domain.Transaction;
import com.example.transactionsservice.transaction.domain.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetTransactionByIdUseCase {

  private final TransactionRepository transactionRepository;

  public GetTransactionByIdUseCase(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Mono<Transaction> execute(String id) {
    return transactionRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Transaction not found")));
  }
}