package com.example.accountsservice.account.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository {

  Flux<Account> findAll();

  Flux<Account> findPage(int page, int size);

  Mono<Account> findById(String id);
}