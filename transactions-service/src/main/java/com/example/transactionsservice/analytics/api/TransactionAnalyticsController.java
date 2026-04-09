package com.example.transactionsservice.analytics.api;

import com.example.transactionsservice.analytics.api.dto.MonthlySummaryResponse;
import com.example.transactionsservice.analytics.api.dto.RunningBalanceResponse;
import com.example.transactionsservice.analytics.api.dto.TopAccountResponse;
import com.example.transactionsservice.analytics.application.GetAnomaliesUseCase;
import com.example.transactionsservice.analytics.application.GetMonthlySummaryUseCase;
import com.example.transactionsservice.analytics.application.GetRunningBalanceUseCase;
import com.example.transactionsservice.analytics.application.GetTopAccountsUseCase;
import com.example.transactionsservice.analytics.application.SearchTransactionsUseCase;
import com.example.transactionsservice.transaction.api.dto.TransactionResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/transactions")
public class TransactionAnalyticsController {

  private final GetRunningBalanceUseCase getRunningBalanceUseCase;
  private final GetMonthlySummaryUseCase getMonthlySummaryUseCase;
  private final GetTopAccountsUseCase getTopAccountsUseCase;
  private final GetAnomaliesUseCase getAnomaliesUseCase;
  private final SearchTransactionsUseCase searchTransactionsUseCase;

  public TransactionAnalyticsController(
      GetRunningBalanceUseCase getRunningBalanceUseCase,
      GetMonthlySummaryUseCase getMonthlySummaryUseCase,
      GetTopAccountsUseCase getTopAccountsUseCase,
      GetAnomaliesUseCase getAnomaliesUseCase,
      SearchTransactionsUseCase searchTransactionsUseCase) {
    this.getRunningBalanceUseCase = getRunningBalanceUseCase;
    this.getMonthlySummaryUseCase = getMonthlySummaryUseCase;
    this.getTopAccountsUseCase = getTopAccountsUseCase;
    this.getAnomaliesUseCase = getAnomaliesUseCase;
    this.searchTransactionsUseCase = searchTransactionsUseCase;
  }

  @GetMapping("/{accountId}/running-balance")
  public Flux<RunningBalanceResponse> runningBalance(@PathVariable String accountId) {
    return getRunningBalanceUseCase.execute(accountId).map(RunningBalanceResponse::fromProjection);
  }

  @GetMapping("/{accountId}/monthly-summary")
  public Flux<MonthlySummaryResponse> monthlySummary(@PathVariable String accountId) {
    return getMonthlySummaryUseCase.execute(accountId).map(MonthlySummaryResponse::fromProjection);
  }

  @GetMapping("/top-accounts")
  public Flux<TopAccountResponse> topAccounts(@RequestParam(defaultValue = "10") int limit) {
    return getTopAccountsUseCase.execute(limit).map(TopAccountResponse::fromProjection);
  }

  @GetMapping("/{accountId}/anomalies")
  public Flux<TransactionResponse> anomalies(@PathVariable String accountId) {
    return getAnomaliesUseCase.execute(accountId).map(TransactionResponse::fromDomain);
  }

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