package com.example.accountsservice.account.infrastructure.persistence;

import com.example.accountsservice.account.domain.Account;
import com.example.accountsservice.account.domain.AccountRepository;
import com.example.accountsservice.account.domain.AccountType;
import java.math.BigDecimal;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresAccountRepository implements AccountRepository {

  private final DatabaseClient databaseClient;

  public PostgresAccountRepository(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Flux<Account> findAll() {
    return databaseClient
        .sql("SELECT id, account_number, type, balance, currency, owner_id FROM accounts ORDER BY id")
        .map(
            (row, rowMetadata) ->
                new Account(
                    row.get("id", String.class),
                    row.get("account_number", String.class),
                    AccountType.valueOf(row.get("type", String.class)),
                    row.get("balance", BigDecimal.class),
                    row.get("currency", String.class),
                    row.get("owner_id", String.class)))
        .all();
  }

  @Override
  public Flux<Account> findPage(int page, int size) {
    return databaseClient
        .sql(
            "SELECT id, account_number, type, balance, currency, owner_id FROM accounts ORDER BY id LIMIT :size OFFSET :offset")
        .bind("size", size)
        .bind("offset", page * size)
        .map(
            (row, rowMetadata) ->
                new Account(
                    row.get("id", String.class),
                    row.get("account_number", String.class),
                    AccountType.valueOf(row.get("type", String.class)),
                    row.get("balance", BigDecimal.class),
                    row.get("currency", String.class),
                    row.get("owner_id", String.class)))
        .all();
  }

  @Override
  public Mono<Account> findById(String id) {
    return databaseClient
        .sql("SELECT id, account_number, type, balance, currency, owner_id FROM accounts WHERE id = :id")
        .bind("id", id)
        .map(
            (row, rowMetadata) ->
                new Account(
                    row.get("id", String.class),
                    row.get("account_number", String.class),
                    AccountType.valueOf(row.get("type", String.class)),
                    row.get("balance", BigDecimal.class),
                    row.get("currency", String.class),
                    row.get("owner_id", String.class)))
        .one();
  }
}