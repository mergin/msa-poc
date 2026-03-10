package com.example.customersservice.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class InMemoryCustomerRepositoryTest {

  private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();

  @Test
  void shouldReturnSeededCustomers() {
    StepVerifier.create(repository.findAll().collectList())
        .assertNext(customers -> assertThat(customers).hasSize(5))
        .verifyComplete();
  }

  @Test
  void shouldFindCustomerById() {
    StepVerifier.create(repository.findById("c-001"))
        .assertNext(customer -> assertThat(customer.id()).isEqualTo("c-001"))
        .verifyComplete();

    StepVerifier.create(repository.findById("missing")).verifyComplete();
  }

  @Test
  void shouldReturnPagedCustomers() {
    StepVerifier.create(repository.findPage(1, 2).collectList())
        .assertNext(customers -> {
          assertThat(customers).hasSize(2);
          assertThat(customers.get(0).id()).isEqualTo("c-003");
        })
        .verifyComplete();
  }
}
