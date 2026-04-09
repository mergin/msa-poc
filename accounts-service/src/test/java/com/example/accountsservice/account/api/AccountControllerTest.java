package com.example.accountsservice.account.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.accountsservice.account.api.exception.ResourceNotFoundException;
import com.example.accountsservice.account.application.GetAccountByIdUseCase;
import com.example.accountsservice.account.application.ListAccountsUseCase;
import com.example.accountsservice.account.domain.Account;
import com.example.accountsservice.account.domain.AccountType;
import com.example.accountsservice.shared.api.ApiExceptionHandler;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(AccountController.class)
@Import(ApiExceptionHandler.class)
class AccountControllerTest {

  @Autowired private WebTestClient webTestClient;

  @MockBean private ListAccountsUseCase listAccountsUseCase;
  @MockBean private GetAccountByIdUseCase getAccountByIdUseCase;

  @Test
  void shouldReturnAccountsList() {
    when(listAccountsUseCase.execute(eq(0), eq(20)))
        .thenReturn(
            Flux.just(
                new Account(
                    "a-001",
                    "ES12-0049-0001",
                    AccountType.CHECKING,
                    new BigDecimal("4250.75"),
                    "EUR",
                    "c-001")));

    webTestClient
        .get()
        .uri("/accounts")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].id")
        .isEqualTo("a-001")
        .jsonPath("$[0].type")
        .isEqualTo("checking");
  }

  @Test
  void shouldReturnAccountsListWithPaginationParams() {
    when(listAccountsUseCase.execute(eq(2), eq(5)))
        .thenReturn(
            Flux.just(
                new Account(
                    "a-011",
                    "ES12-0049-0011",
                    AccountType.SAVINGS,
                    new BigDecimal("508.65"),
                    "EUR",
                    "c-011")));

    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/accounts").queryParam("page", 2).queryParam("size", 5).build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].id")
        .isEqualTo("a-011");
  }

  @Test
  void shouldReturnNotFoundForMissingAccount() {
    when(getAccountByIdUseCase.execute("missing"))
        .thenReturn(Mono.error(new ResourceNotFoundException("Account not found")));

    webTestClient
        .get()
        .uri("/accounts/missing")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.message")
        .isEqualTo("Account not found");
  }
}