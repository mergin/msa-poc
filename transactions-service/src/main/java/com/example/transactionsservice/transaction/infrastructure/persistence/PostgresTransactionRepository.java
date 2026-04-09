package com.example.transactionsservice.transaction.infrastructure.persistence;

import com.example.transactionsservice.transaction.domain.Transaction;
import com.example.transactionsservice.transaction.domain.TransactionRepository;
import com.example.transactionsservice.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresTransactionRepository implements TransactionRepository {

  private final DatabaseClient db;

  public PostgresTransactionRepository(DatabaseClient db) {
    this.db = db;
  }

  static Transaction mapTransaction(io.r2dbc.spi.Row row) {
    return new Transaction(
        row.get("id", String.class),
        row.get("account_id", String.class),
        TransactionType.valueOf(row.get("type", String.class)),
        row.get("amount", BigDecimal.class),
        row.get("currency", String.class),
        row.get("description", String.class),
        row.get("timestamp", LocalDateTime.class),
        row.get("category", String.class));
  }

  @Override
  public Flux<Transaction> findPage(int page, int size) {
    return db.sql(
            """
            SELECT id, account_id, type, amount, currency, description, timestamp, category
            FROM transactions
            ORDER BY timestamp DESC
            LIMIT :size OFFSET :offset
            """)
        .bind("size", size)
        .bind("offset", page * size)
        .map((row, meta) -> mapTransaction(row))
        .all();
  }

  @Override
  public Mono<Transaction> findById(String id) {
    return db.sql(
            """
            SELECT id, account_id, type, amount, currency, description, timestamp, category
            FROM transactions
            WHERE id = :id
            """)
        .bind("id", id)
        .map((row, meta) -> mapTransaction(row))
        .one();
  }
}