package com.example.accountsservice.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.accountsservice.domain.model.Account;
import com.example.accountsservice.domain.model.AccountType;
import com.example.accountsservice.domain.repository.AccountRepository;
import com.example.accountsservice.interfaces.rest.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GetAccountByIdUseCaseTest {

  private final AccountRepository repository = Mockito.mock(AccountRepository.class);
  private final GetAccountByIdUseCase useCase = new GetAccountByIdUseCase(repository);

  @Test
  void shouldReturnAccountWhenFound() {
    Account account =
        new Account(
            "a-1", "ES12-0000-0001", AccountType.CHECKING, new BigDecimal("100.00"), "EUR", "c-1");
    when(repository.findById("a-1")).thenReturn(Mono.just(account));

    StepVerifier.create(useCase.execute("a-1"))
      .assertNext(result -> assertThat(result.id()).isEqualTo("a-1"))
      .verifyComplete();
  }

  @Test
  void shouldThrowNotFoundWhenAccountDoesNotExist() {
    when(repository.findById("missing")).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute("missing"))
      .expectErrorMatches(
        error ->
          error instanceof ResourceNotFoundException
            && "Account not found".equals(error.getMessage()))
      .verify();
  }
}
