package com.example.transactionsservice.interfaces.rest;

import com.example.transactionsservice.application.usecase.GetAnomaliesUseCase;
import com.example.transactionsservice.application.usecase.GetMonthlySummaryUseCase;
import com.example.transactionsservice.application.usecase.GetRunningBalanceUseCase;
import com.example.transactionsservice.application.usecase.GetTopAccountsUseCase;
import com.example.transactionsservice.application.usecase.GetTransactionByIdUseCase;
import com.example.transactionsservice.application.usecase.ListTransactionsUseCase;
import com.example.transactionsservice.application.usecase.SearchTransactionsUseCase;
import com.example.transactionsservice.interfaces.rest.dto.MonthlySummaryResponse;
import com.example.transactionsservice.interfaces.rest.dto.RunningBalanceResponse;
import com.example.transactionsservice.interfaces.rest.dto.TopAccountResponse;
import com.example.transactionsservice.interfaces.rest.dto.TransactionResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
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
  private final GetRunningBalanceUseCase getRunningBalanceUseCase;
  private final GetMonthlySummaryUseCase getMonthlySummaryUseCase;
  private final GetTopAccountsUseCase getTopAccountsUseCase;
  private final GetAnomaliesUseCase getAnomaliesUseCase;
  private final SearchTransactionsUseCase searchTransactionsUseCase;

  public TransactionController(
      ListTransactionsUseCase listTransactionsUseCase,
      GetTransactionByIdUseCase getTransactionByIdUseCase,
      GetRunningBalanceUseCase getRunningBalanceUseCase,
      GetMonthlySummaryUseCase getMonthlySummaryUseCase,
      GetTopAccountsUseCase getTopAccountsUseCase,
      GetAnomaliesUseCase getAnomaliesUseCase,
      SearchTransactionsUseCase searchTransactionsUseCase) {
    this.listTransactionsUseCase = listTransactionsUseCase;
    this.getTransactionByIdUseCase = getTransactionByIdUseCase;
    this.getRunningBalanceUseCase = getRunningBalanceUseCase;
    this.getMonthlySummaryUseCase = getMonthlySummaryUseCase;
    this.getTopAccountsUseCase = getTopAccountsUseCase;
    this.getAnomaliesUseCase = getAnomaliesUseCase;
    this.searchTransactionsUseCase = searchTransactionsUseCase;
  }

  /** Paginated list of all transactions, newest first. */
  @GetMapping
  public Flux<TransactionResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return listTransactionsUseCase.execute(page, size).map(TransactionResponse::fromDomain);
  }

  /** Single transaction by ID. Returns 404 if not found. */
  @GetMapping("/{id}")
  public Mono<TransactionResponse> getById(@PathVariable String id) {
    return getTransactionByIdUseCase.execute(id).map(TransactionResponse::fromDomain);
  }

  /**
   * Running balance for an account — one row per transaction with the cumulative
   * signed balance computed via a SQL window function.
   */
  @GetMapping("/{accountId}/running-balance")
  public Flux<RunningBalanceResponse> runningBalance(@PathVariable String accountId) {
    return getRunningBalanceUseCase.execute(accountId).map(RunningBalanceResponse::fromProjection);
  }

  /**
   * Monthly debit / credit / net summary for an account.
   * One row per calendar month, ordered chronologically.
   */
  @GetMapping("/{accountId}/monthly-summary")
  public Flux<MonthlySummaryResponse> monthlySummary(@PathVariable String accountId) {
    return getMonthlySummaryUseCase
        .execute(accountId)
        .map(MonthlySummaryResponse::fromProjection);
  }

  /**
   * Top N accounts ranked by total transaction volume.
   * Defaults to top 10; capped at 50.
   */
  @GetMapping("/top-accounts")
  public Flux<TopAccountResponse> topAccounts(
      @RequestParam(defaultValue = "10") int limit) {
    return getTopAccountsUseCase.execute(limit).map(TopAccountResponse::fromProjection);
  }

  /**
   * Statistical outliers for an account — transactions whose amount exceeds
   * the per-account mean by more than two standard deviations.
   */
  @GetMapping("/{accountId}/anomalies")
  public Flux<TransactionResponse> anomalies(@PathVariable String accountId) {
    return getAnomaliesUseCase.execute(accountId).map(TransactionResponse::fromDomain);
  }

  /**
   * Multi-criteria search across all transactions.
   * All filter parameters are optional and combinable.
   *
   * @param category  exact category match (e.g. GROCERIES)
   * @param from      ISO-8601 lower bound on timestamp (inclusive)
   * @param to        ISO-8601 upper bound on timestamp (inclusive)
   * @param minAmount minimum transaction amount (inclusive)
   * @param page      zero-based page number (default 0)
   * @param size      page size (default 20, max 100)
   */
  @GetMapping("/search")
  public Flux<TransactionResponse> search(
      @RequestParam(required = false) String category,
      @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime from,
      @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime to,
      @RequestParam(required = false) BigDecimal minAmount,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return searchTransactionsUseCase
        .execute(category, from, to, minAmount, page, size)
        .map(TransactionResponse::fromDomain);
  }
}
