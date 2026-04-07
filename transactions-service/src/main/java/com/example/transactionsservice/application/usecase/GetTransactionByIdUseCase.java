package com.example.transactionsservice.application.usecase;

import com.example.transactionsservice.domain.model.Transaction;
import com.example.transactionsservice.domain.repository.TransactionRepository;
import com.example.transactionsservice.interfaces.rest.exception.ResourceNotFoundException;
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
