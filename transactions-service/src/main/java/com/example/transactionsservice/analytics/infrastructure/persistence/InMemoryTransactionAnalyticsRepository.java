package com.example.transactionsservice.analytics.infrastructure.persistence;

import com.example.transactionsservice.analytics.domain.MonthlySummaryRow;
import com.example.transactionsservice.analytics.domain.RunningBalanceRow;
import com.example.transactionsservice.analytics.domain.TopAccountRow;
import com.example.transactionsservice.analytics.domain.TransactionAnalyticsRepository;
import com.example.transactionsservice.transaction.domain.Transaction;
import com.example.transactionsservice.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@Profile("in-memory")
public class InMemoryTransactionAnalyticsRepository implements TransactionAnalyticsRepository {

  private static final List<Transaction> DATA = InMemoryTransactionData.DATA;

  @Override
  public Flux<RunningBalanceRow> findRunningBalanceByAccountId(String accountId) {
    List<Transaction> sorted =
        DATA.stream()
            .filter(t -> t.accountId().equals(accountId))
            .sorted(Comparator.comparing(Transaction::timestamp))
            .toList();

    List<RunningBalanceRow> result = new java.util.ArrayList<>();
    BigDecimal running = BigDecimal.ZERO;
    for (Transaction t : sorted) {
      BigDecimal signed = t.type() == TransactionType.CREDIT ? t.amount() : t.amount().negate();
      running = running.add(signed);
      result.add(
          new RunningBalanceRow(
              t.accountId(), t.timestamp(), t.type().name(), t.amount(), t.description(), running));
    }
    return Flux.fromIterable(result);
  }

  @Override
  public Flux<MonthlySummaryRow> findMonthlySummaryByAccountId(String accountId) {
    Map<LocalDateTime, List<Transaction>> byMonth =
        DATA.stream()
            .filter(t -> t.accountId().equals(accountId))
            .collect(
                Collectors.groupingBy(
                    t -> t.timestamp().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)));

    return Flux.fromIterable(
        byMonth.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(
                entry -> {
                  List<Transaction> txs = entry.getValue();
                  BigDecimal credits =
                      txs.stream()
                          .filter(t -> t.type() == TransactionType.CREDIT)
                          .map(Transaction::amount)
                          .reduce(BigDecimal.ZERO, BigDecimal::add);
                  BigDecimal debits =
                      txs.stream()
                          .filter(t -> t.type() == TransactionType.DEBIT)
                          .map(Transaction::amount)
                          .reduce(BigDecimal.ZERO, BigDecimal::add);
                  return new MonthlySummaryRow(
                      entry.getKey(), credits, debits, credits.subtract(debits), txs.size());
                })
            .toList());
  }

  @Override
  public Flux<TopAccountRow> findTopAccounts(int limit) {
    Map<String, List<Transaction>> byAccount =
        DATA.stream().collect(Collectors.groupingBy(Transaction::accountId));

    return Flux.fromIterable(
        byAccount.entrySet().stream()
            .map(
                e ->
                    new TopAccountRow(
                        e.getKey(),
                        e.getValue().stream()
                            .map(Transaction::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add),
                        e.getValue().size()))
            .sorted(Comparator.comparing(TopAccountRow::totalVolume).reversed())
            .limit(limit)
            .toList());
  }

  @Override
  public Flux<Transaction> findAnomaliesByAccountId(String accountId) {
    List<Transaction> accountTxs = DATA.stream().filter(t -> t.accountId().equals(accountId)).toList();
    if (accountTxs.isEmpty()) return Flux.empty();

    BigDecimal mean =
        accountTxs.stream()
            .map(Transaction::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(accountTxs.size()), MathContext.DECIMAL64);

    BigDecimal variance =
        accountTxs.stream()
            .map(t -> t.amount().subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(accountTxs.size()), MathContext.DECIMAL64);

    BigDecimal stddev = variance.sqrt(MathContext.DECIMAL64);
    BigDecimal threshold = mean.add(stddev.multiply(new BigDecimal("2")));

    return Flux.fromIterable(
        accountTxs.stream()
            .filter(t -> t.amount().compareTo(threshold) > 0)
            .sorted(Comparator.comparing(Transaction::amount).reversed())
            .toList());
  }

  @Override
  public Flux<Transaction> search(
      String category,
      LocalDateTime from,
      LocalDateTime to,
      BigDecimal minAmount,
      int page,
      int size) {
    return Flux.fromIterable(
        DATA.stream()
            .filter(t -> category == null || t.category().equalsIgnoreCase(category))
            .filter(t -> from == null || !t.timestamp().isBefore(from))
            .filter(t -> to == null || !t.timestamp().isAfter(to))
            .filter(t -> minAmount == null || t.amount().compareTo(minAmount) >= 0)
            .sorted(Comparator.comparing(Transaction::timestamp).reversed())
            .skip((long) page * size)
            .limit(size)
            .toList());
  }
}