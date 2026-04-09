package com.example.customersservice.customer.infrastructure.persistence;

import com.example.customersservice.customer.domain.Customer;
import com.example.customersservice.customer.domain.CustomerRepository;
import com.example.customersservice.customer.domain.CustomerStatus;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresCustomerRepository implements CustomerRepository {

  private final DatabaseClient databaseClient;

  public PostgresCustomerRepository(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Flux<Customer> findAll() {
    return databaseClient
        .sql("SELECT id, name, email, status FROM customers ORDER BY id")
        .map(
            (row, rowMetadata) ->
                new Customer(
                    row.get("id", String.class),
                    row.get("name", String.class),
                    row.get("email", String.class),
                    CustomerStatus.valueOf(row.get("status", String.class))))
        .all();
  }

  @Override
  public Flux<Customer> findPage(int page, int size) {
    return databaseClient
        .sql("SELECT id, name, email, status FROM customers ORDER BY id LIMIT :size OFFSET :offset")
        .bind("size", size)
        .bind("offset", page * size)
        .map(
            (row, rowMetadata) ->
                new Customer(
                    row.get("id", String.class),
                    row.get("name", String.class),
                    row.get("email", String.class),
                    CustomerStatus.valueOf(row.get("status", String.class))))
        .all();
  }

  @Override
  public Mono<Customer> findById(String id) {
    return databaseClient
        .sql("SELECT id, name, email, status FROM customers WHERE id = :id")
        .bind("id", id)
        .map(
            (row, rowMetadata) ->
                new Customer(
                    row.get("id", String.class),
                    row.get("name", String.class),
                    row.get("email", String.class),
                    CustomerStatus.valueOf(row.get("status", String.class))))
        .one();
  }
}