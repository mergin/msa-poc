package com.example.transactionsservice.transaction.api;

import static org.mockito.Mockito.when;

import com.example.transactionsservice.shared.api.ApiExceptionHandler;
import com.example.transactionsservice.transaction.api.exception.ResourceNotFoundException;
import com.example.transactionsservice.transaction.application.GetTransactionByIdUseCase;
import com.example.transactionsservice.transaction.application.ListTransactionsUseCase;
import com.example.transactionsservice.transaction.domain.Transaction;
import com.example.transactionsservice.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {TransactionController.class, ApiExceptionHandler.class})
class TransactionControllerTest {

  @Autowired private WebTestClient webTestClient;

  @MockitoBean private ListTransactionsUseCase listTransactionsUseCase;
  @MockitoBean private GetTransactionByIdUseCase getTransactionByIdUseCase;

  private static final Transaction TX =
      new Transaction(
          "tx-001",
          "a-001",
          TransactionType.DEBIT,
          new BigDecimal("99.50"),
          "EUR",
          "grocery run",
          LocalDateTime.now().minusDays(1),
          "GROCERIES");

  @Test
  void listReturns200WithTransactions() {
    when(listTransactionsUseCase.execute(0, 20)).thenReturn(Flux.just(TX));

    webTestClient.get().uri("/transactions")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].id").isEqualTo("tx-001")
        .jsonPath("$[0].type").isEqualTo("debit");
  }

  @Test
  void getByIdReturns200WhenFound() {
    when(getTransactionByIdUseCase.execute("tx-001")).thenReturn(Mono.just(TX));

    webTestClient.get().uri("/transactions/tx-001")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo("tx-001")
        .jsonPath("$.category").isEqualTo("GROCERIES");
  }

  @Test
  void getByIdReturns404WhenNotFound() {
    when(getTransactionByIdUseCase.execute("missing"))
        .thenReturn(Mono.error(new ResourceNotFoundException("Transaction not found")));

    webTestClient.get().uri("/transactions/missing")
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.message").isEqualTo("Transaction not found");
  }
}