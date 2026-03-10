package com.example.accountsservice.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class InMemoryAccountRepositoryTest {

  private final InMemoryAccountRepository repository = new InMemoryAccountRepository();

  @Test
  void shouldReturnSeededAccounts() {
    StepVerifier.create(repository.findAll().collectList())
        .assertNext(accounts -> assertThat(accounts).hasSize(6))
        .verifyComplete();
  }

  @Test
  void shouldFindAccountById() {
    StepVerifier.create(repository.findById("a-001"))
        .assertNext(account -> assertThat(account.id()).isEqualTo("a-001"))
        .verifyComplete();

    StepVerifier.create(repository.findById("missing")).verifyComplete();
  }

  @Test
  void shouldReturnPagedAccounts() {
    StepVerifier.create(repository.findPage(1, 2).collectList())
        .assertNext(accounts -> {
          assertThat(accounts).hasSize(2);
          assertThat(accounts.get(0).id()).isEqualTo("a-003");
        })
        .verifyComplete();
  }
}
