package com.example.accountsservice.account.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.accountsservice.account.domain.Account;
import com.example.accountsservice.account.domain.AccountRepository;
import com.example.accountsservice.account.domain.AccountType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class ListAccountsUseCaseTest {

  private final AccountRepository repository = Mockito.mock(AccountRepository.class);
  private final ListAccountsUseCase useCase = new ListAccountsUseCase(repository);

  @Test
  void shouldReturnAllAccounts() {
    when(repository.findPage(0, 20))
        .thenReturn(
            Flux.just(
                new Account(
                    "a-1",
                    "ES12-0000-0001",
                    AccountType.CHECKING,
                    new BigDecimal("100.00"),
                    "EUR",
                    "c-1")));

    StepVerifier.create(useCase.execute(0, 20))
        .assertNext(account -> assertThat(account.id()).isEqualTo("a-1"))
        .verifyComplete();
  }
}