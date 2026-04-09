package com.example.customersservice.customer.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository {

  Flux<Customer> findAll();

  Flux<Customer> findPage(int page, int size);

  Mono<Customer> findById(String id);
}