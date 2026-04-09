package com.example.transactionsservice.transaction.infrastructure.persistence;

import com.example.transactionsservice.transaction.domain.Transaction;
import com.example.transactionsservice.transaction.domain.TransactionRepository;
import java.util.Comparator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Profile("in-memory")
public class InMemoryTransactionRepository implements TransactionRepository {

  @Override
  public Flux<Transaction> findPage(int page, int size) {
    return Flux.fromIterable(
        InMemoryTransactionData.DATA.stream()
            .sorted(Comparator.comparing(Transaction::timestamp).reversed())
            .skip((long) page * size)
            .limit(size)
            .toList());
  }

  @Override
  public Mono<Transaction> findById(String id) {
    return Mono.justOrEmpty(InMemoryTransactionData.DATA.stream().filter(t -> t.id().equals(id)).findFirst());
  }
}