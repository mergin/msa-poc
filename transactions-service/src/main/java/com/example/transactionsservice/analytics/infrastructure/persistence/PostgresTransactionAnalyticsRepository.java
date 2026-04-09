package com.example.transactionsservice.analytics.infrastructure.persistence;

import com.example.transactionsservice.analytics.domain.MonthlySummaryRow;
import com.example.transactionsservice.analytics.domain.RunningBalanceRow;
import com.example.transactionsservice.analytics.domain.TopAccountRow;
import com.example.transactionsservice.analytics.domain.TransactionAnalyticsRepository;
import com.example.transactionsservice.transaction.domain.Transaction;
import com.example.transactionsservice.transaction.infrastructure.persistence.PostgresTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class PostgresTransactionAnalyticsRepository implements TransactionAnalyticsRepository {

  private final DatabaseClient db;

  public PostgresTransactionAnalyticsRepository(DatabaseClient db) {
    this.db = db;
  }

  @Override
  public Flux<RunningBalanceRow> findRunningBalanceByAccountId(String accountId) {
    return db.sql(
            """
            SELECT
              account_id,
              timestamp,
              type,
              amount,
              description,
              SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END)
                OVER (
                  PARTITION BY account_id
                  ORDER BY timestamp
                  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                ) AS running_balance
            FROM transactions
            WHERE account_id = :accountId
            ORDER BY timestamp
            """)
        .bind("accountId", accountId)
        .map(
            (row, meta) ->
                new RunningBalanceRow(
                    row.get("account_id", String.class),
                    row.get("timestamp", LocalDateTime.class),
                    row.get("type", String.class),
                    row.get("amount", BigDecimal.class),
                    row.get("description", String.class),
                    row.get("running_balance", BigDecimal.class)))
        .all();
  }

  @Override
  public Flux<MonthlySummaryRow> findMonthlySummaryByAccountId(String accountId) {
    return db.sql(
            """
            SELECT
              DATE_TRUNC('month', timestamp)                                    AS month,
              SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE 0     END)        AS total_credits,
              SUM(CASE WHEN type = 'DEBIT'  THEN amount ELSE 0     END)        AS total_debits,
              SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END)      AS net_amount,
              COUNT(*)                                                           AS transaction_count
            FROM transactions
            WHERE account_id = :accountId
            GROUP BY DATE_TRUNC('month', timestamp)
            ORDER BY month
            """)
        .bind("accountId", accountId)
        .map(
            (row, meta) ->
                new MonthlySummaryRow(
                    row.get("month", LocalDateTime.class),
                    row.get("total_credits", BigDecimal.class),
                    row.get("total_debits", BigDecimal.class),
                    row.get("net_amount", BigDecimal.class),
                    row.get("transaction_count", Long.class)))
        .all();
  }

  @Override
  public Flux<TopAccountRow> findTopAccounts(int limit) {
    return db.sql(
            """
            SELECT
              account_id,
              SUM(amount)  AS total_volume,
              COUNT(*)     AS transaction_count
            FROM transactions
            GROUP BY account_id
            ORDER BY total_volume DESC
            LIMIT :limit
            """)
        .bind("limit", limit)
        .map(
            (row, meta) ->
                new TopAccountRow(
                    row.get("account_id", String.class),
                    row.get("total_volume", BigDecimal.class),
                    row.get("transaction_count", Long.class)))
        .all();
  }

  @Override
  public Flux<Transaction> findAnomaliesByAccountId(String accountId) {
    return db.sql(
            """
            SELECT t.id, t.account_id, t.type, t.amount, t.currency,
                   t.description, t.timestamp, t.category
            FROM transactions t
            JOIN (
              SELECT account_id,
                     AVG(amount)    AS mean_amount,
                     STDDEV(amount) AS stddev_amount
              FROM transactions
              WHERE account_id = :accountId
              GROUP BY account_id
            ) stats ON t.account_id = stats.account_id
            WHERE t.account_id = :accountId
              AND t.amount > (stats.mean_amount + 2 * COALESCE(stats.stddev_amount, 0))
            ORDER BY t.amount DESC
            """)
        .bind("accountId", accountId)
        .map((row, meta) -> PostgresTransactionRepository.mapTransaction(row))
        .all();
  }

  @Override
  public Flux<Transaction> search(
      String category,
      LocalDateTime from,
      LocalDateTime to,
      BigDecimal minAmount,
      int page,
      int size) {
    List<String> predicates = new ArrayList<>();
    if (category != null) predicates.add("category = :category");
    if (from != null) predicates.add("timestamp >= :from");
    if (to != null) predicates.add("timestamp <= :to");
    if (minAmount != null) predicates.add("amount >= :minAmount");

    String where = predicates.isEmpty() ? "" : "WHERE " + String.join(" AND ", predicates);

    String sql =
        String.format(
            """
            SELECT id, account_id, type, amount, currency, description, timestamp, category
            FROM transactions
            %s
            ORDER BY timestamp DESC
            LIMIT :size OFFSET :offset
            """,
            where);

    DatabaseClient.GenericExecuteSpec spec = db.sql(sql);
    if (category != null) spec = spec.bind("category", category);
    if (from != null) spec = spec.bind("from", from);
    if (to != null) spec = spec.bind("to", to);
    if (minAmount != null) spec = spec.bind("minAmount", minAmount);
    spec = spec.bind("size", size).bind("offset", page * size);

    return spec.map((row, meta) -> PostgresTransactionRepository.mapTransaction(row)).all();
  }
}