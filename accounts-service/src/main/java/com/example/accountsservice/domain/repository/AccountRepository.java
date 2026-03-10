package com.example.accountsservice.domain.repository;

import com.example.accountsservice.domain.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository {

  Flux<Account> findAll();

  Flux<Account> findPage(int page, int size);

  Mono<Account> findById(String id);
}
