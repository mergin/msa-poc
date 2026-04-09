package com.example.transactionsservice.transaction.api;

import com.example.transactionsservice.transaction.api.dto.TransactionResponse;
import com.example.transactionsservice.transaction.application.GetTransactionByIdUseCase;
import com.example.transactionsservice.transaction.application.ListTransactionsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

  private final ListTransactionsUseCase listTransactionsUseCase;
  private final GetTransactionByIdUseCase getTransactionByIdUseCase;

  public TransactionController(
      ListTransactionsUseCase listTransactionsUseCase,
      GetTransactionByIdUseCase getTransactionByIdUseCase) {
    this.listTransactionsUseCase = listTransactionsUseCase;
    this.getTransactionByIdUseCase = getTransactionByIdUseCase;
  }

  @GetMapping
  public Flux<TransactionResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return listTransactionsUseCase.execute(page, size).map(TransactionResponse::fromDomain);
  }

  @GetMapping("/{id}")
  public Mono<TransactionResponse> getById(@PathVariable String id) {
    return getTransactionByIdUseCase.execute(id).map(TransactionResponse::fromDomain);
  }
}