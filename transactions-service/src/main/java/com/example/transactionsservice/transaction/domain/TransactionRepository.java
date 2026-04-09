package com.example.transactionsservice.transaction.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionRepository {

  Flux<Transaction> findPage(int page, int size);

  Mono<Transaction> findById(String id);
}