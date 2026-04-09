package com.example.transactionsservice.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.transactionsservice.transaction.api.exception.ResourceNotFoundException;
import com.example.transactionsservice.transaction.domain.Transaction;
import com.example.transactionsservice.transaction.domain.TransactionRepository;
import com.example.transactionsservice.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GetTransactionByIdUseCaseTest {

  private final TransactionRepository repository = Mockito.mock(TransactionRepository.class);
  private final GetTransactionByIdUseCase useCase = new GetTransactionByIdUseCase(repository);

  private static final Transaction TX =
      new Transaction(
          "tx-001", "a-001", TransactionType.DEBIT,
          new BigDecimal("99.50"), "EUR", "grocery run",
          LocalDateTime.now().minusDays(1), "GROCERIES");

  @Test
  void shouldReturnTransactionWhenFound() {
    when(repository.findById("tx-001")).thenReturn(Mono.just(TX));

    StepVerifier.create(useCase.execute("tx-001"))
        .assertNext(t -> assertThat(t.id()).isEqualTo("tx-001"))
        .verifyComplete();
  }

  @Test
  void shouldThrowNotFoundWhenTransactionDoesNotExist() {
    when(repository.findById("missing")).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute("missing"))
        .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException)
        .verify();
  }
}