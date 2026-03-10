package com.example.customersservice.domain.repository;

import com.example.customersservice.domain.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository {

  Flux<Customer> findAll();

  Flux<Customer> findPage(int page, int size);

  Mono<Customer> findById(String id);
}
