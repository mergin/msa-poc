package com.example.accountsservice.application.usecase;

import static org.mockito.Mockito.when;

import com.example.accountsservice.domain.model.Account;
import com.example.accountsservice.domain.model.AccountType;
import com.example.accountsservice.domain.repository.AccountRepository;
import com.example.accountsservice.infrastructure.client.CustomersClient;
import com.example.accountsservice.infrastructure.client.dto.CustomerSummary;
import com.example.accountsservice.interfaces.rest.exception.ResourceNotFoundException;
import com.example.accountsservice.interfaces.rest.exception.ServiceUnavailableException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GetAccountOwnerUseCaseTest {

  private final AccountRepository accountRepository = Mockito.mock(AccountRepository.class);
  private final CustomersClient customersClient = Mockito.mock(CustomersClient.class);
  private final GetAccountOwnerUseCase useCase =
      new GetAccountOwnerUseCase(accountRepository, customersClient);

  private static final Account ACCOUNT =
      new Account(
          "a-1", "ES12-0000-0001", AccountType.CHECKING, new BigDecimal("100.00"), "EUR", "c-1");

  private static final CustomerSummary OWNER =
      new CustomerSummary("c-1", "Alice", "Smith", "alice@example.com");

  @Test
  void shouldReturnEnrichedOwnerResponse() {
    when(accountRepository.findById("a-1")).thenReturn(Mono.just(ACCOUNT));
    when(customersClient.findById("c-1")).thenReturn(Mono.just(OWNER));

    StepVerifier.create(useCase.execute("a-1"))
        .assertNext(
            result -> {
              assert result.accountId().equals("a-1");
              assert result.ownerFirstName().equals("Alice");
              assert result.ownerEmail().equals("alice@example.com");
            })
        .verifyComplete();
  }

  @Test
  void shouldErrorWhenAccountNotFound() {
    when(accountRepository.findById("missing")).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute("missing"))
        .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException)
        .verify();
  }

  @Test
  void shouldErrorWhenCustomersServiceUnavailable() {
    when(accountRepository.findById("a-1")).thenReturn(Mono.just(ACCOUNT));
    when(customersClient.findById("c-1")).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute("a-1"))
        .expectErrorMatches(ex -> ex instanceof ServiceUnavailableException)
        .verify();
  }
}
