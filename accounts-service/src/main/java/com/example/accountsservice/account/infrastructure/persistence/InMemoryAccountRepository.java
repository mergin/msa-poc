package com.example.accountsservice.account.infrastructure.persistence;

import com.example.accountsservice.account.domain.Account;
import com.example.accountsservice.account.domain.AccountRepository;
import com.example.accountsservice.account.domain.AccountType;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Profile("in-memory")
public class InMemoryAccountRepository implements AccountRepository {

  private final List<Account> accounts =
      List.of(
          new Account(
              "a-001",
              "ES12-0049-0001",
              AccountType.CHECKING,
              new BigDecimal("4250.75"),
              "EUR",
              "c-001"),
          new Account(
              "a-002",
              "ES12-0049-0002",
              AccountType.SAVINGS,
              new BigDecimal("18900.00"),
              "EUR",
              "c-001"),
          new Account(
              "a-003",
              "ES12-0049-0003",
              AccountType.CHECKING,
              new BigDecimal("1035.20"),
              "EUR",
              "c-002"),
          new Account(
              "a-004",
              "ES12-0049-0004",
              AccountType.CREDIT,
              new BigDecimal("-540.00"),
              "EUR",
              "c-002"),
          new Account(
              "a-005",
              "ES12-0049-0005",
              AccountType.SAVINGS,
              new BigDecimal("7600.00"),
              "EUR",
              "c-003"),
          new Account(
              "a-006",
              "ES12-0049-0006",
              AccountType.CHECKING,
              new BigDecimal("320.10"),
              "EUR",
              "c-004"));

  @Override
  public Flux<Account> findAll() {
    return Flux.fromIterable(accounts);
  }

  @Override
  public Flux<Account> findPage(int page, int size) {
    return Flux.fromStream(accounts.stream().skip((long) page * size).limit(size));
  }

  @Override
  public Mono<Account> findById(String id) {
    return Mono.justOrEmpty(accounts.stream().filter(account -> account.id().equals(id)).findFirst());
  }
}