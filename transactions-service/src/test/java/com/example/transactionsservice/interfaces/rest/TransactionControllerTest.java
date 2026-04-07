package com.example.transactionsservice.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.transactionsservice.application.usecase.GetAnomaliesUseCase;
import com.example.transactionsservice.application.usecase.GetMonthlySummaryUseCase;
import com.example.transactionsservice.application.usecase.GetRunningBalanceUseCase;
import com.example.transactionsservice.application.usecase.GetTopAccountsUseCase;
import com.example.transactionsservice.application.usecase.GetTransactionByIdUseCase;
import com.example.transactionsservice.application.usecase.ListTransactionsUseCase;
import com.example.transactionsservice.application.usecase.SearchTransactionsUseCase;
import com.example.transactionsservice.domain.model.Transaction;
import com.example.transactionsservice.domain.model.TransactionType;
import com.example.transactionsservice.domain.model.projection.MonthlySummaryRow;
import com.example.transactionsservice.domain.model.projection.RunningBalanceRow;
import com.example.transactionsservice.domain.model.projection.TopAccountRow;
import com.example.transactionsservice.interfaces.rest.exception.ResourceNotFoundException;
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
  @MockitoBean private GetRunningBalanceUseCase getRunningBalanceUseCase;
  @MockitoBean private GetMonthlySummaryUseCase getMonthlySummaryUseCase;
  @MockitoBean private GetTopAccountsUseCase getTopAccountsUseCase;
  @MockitoBean private GetAnomaliesUseCase getAnomaliesUseCase;
  @MockitoBean private SearchTransactionsUseCase searchTransactionsUseCase;

  private static final Transaction TX =
      new Transaction(
          "tx-001", "a-001", TransactionType.DEBIT,
          new BigDecimal("99.50"), "EUR", "grocery run",
          LocalDateTime.now().minusDays(1), "GROCERIES");

  // -----------------------------------------------------------------------
  // GET /transactions
  // -----------------------------------------------------------------------

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

  // -----------------------------------------------------------------------
  // GET /transactions/{id}
  // -----------------------------------------------------------------------

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

  // -----------------------------------------------------------------------
  // GET /transactions/{accountId}/running-balance
  // -----------------------------------------------------------------------

  @Test
  void runningBalanceReturns200WithRows() {
    RunningBalanceRow row = new RunningBalanceRow(
        "a-001", LocalDateTime.now(), "DEBIT",
        new BigDecimal("99.50"), "grocery run", new BigDecimal("-99.50"));
    when(getRunningBalanceUseCase.execute("a-001")).thenReturn(Flux.just(row));

    webTestClient.get().uri("/transactions/a-001/running-balance")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].runningBalance").isEqualTo(-99.50);
  }

  // -----------------------------------------------------------------------
  // GET /transactions/{accountId}/monthly-summary
  // -----------------------------------------------------------------------

  @Test
  void monthlySummaryReturns200() {
    MonthlySummaryRow row = new MonthlySummaryRow(
        LocalDateTime.now().withDayOfMonth(1),
        new BigDecimal("500"), new BigDecimal("300"), new BigDecimal("200"), 5L);
    when(getMonthlySummaryUseCase.execute("a-001")).thenReturn(Flux.just(row));

    webTestClient.get().uri("/transactions/a-001/monthly-summary")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].transactionCount").isEqualTo(5);
  }

  // -----------------------------------------------------------------------
  // GET /transactions/top-accounts
  // -----------------------------------------------------------------------

  @Test
  void topAccountsReturns200() {
    TopAccountRow row = new TopAccountRow("a-001", new BigDecimal("9999"), 42L);
    when(getTopAccountsUseCase.execute(10)).thenReturn(Flux.just(row));

    webTestClient.get().uri("/transactions/top-accounts")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].accountId").isEqualTo("a-001")
        .jsonPath("$[0].transactionCount").isEqualTo(42);
  }

  // -----------------------------------------------------------------------
  // GET /transactions/{accountId}/anomalies
  // -----------------------------------------------------------------------

  @Test
  void anomaliesReturns200() {
    when(getAnomaliesUseCase.execute("a-001")).thenReturn(Flux.just(TX));

    webTestClient.get().uri("/transactions/a-001/anomalies")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].id").isEqualTo("tx-001");
  }

  // -----------------------------------------------------------------------
  // GET /transactions/search
  // -----------------------------------------------------------------------

  @Test
  void searchReturns200WithMatchingTransactions() {
    when(searchTransactionsUseCase.execute(eq("GROCERIES"), any(), any(), any(), eq(0), eq(20)))
        .thenReturn(Flux.just(TX));

    webTestClient.get().uri("/transactions/search?category=GROCERIES")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].category").isEqualTo("GROCERIES");
  }

  @Test
  void searchReturnsEmptyArrayWhenNoMatches() {
    when(searchTransactionsUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(Flux.empty());

    webTestClient.get().uri("/transactions/search?category=UNKNOWN")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$").isEmpty();
  }
}
