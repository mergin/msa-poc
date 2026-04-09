package com.example.customersservice.customer.infrastructure.persistence;

import com.example.customersservice.customer.domain.Customer;
import com.example.customersservice.customer.domain.CustomerRepository;
import com.example.customersservice.customer.domain.CustomerStatus;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Profile("in-memory")
public class InMemoryCustomerRepository implements CustomerRepository {

  private final List<Customer> customers =
      List.of(
          new Customer("c-001", "Alice Martínez", "alice@example.com", CustomerStatus.ACTIVE),
          new Customer("c-002", "Bob Nguyen", "bob@example.com", CustomerStatus.ACTIVE),
          new Customer("c-003", "Carol Obi", "carol@example.com", CustomerStatus.INACTIVE),
          new Customer("c-004", "David Kim", "david@example.com", CustomerStatus.ACTIVE),
          new Customer("c-005", "Eva Rossi", "eva@example.com", CustomerStatus.INACTIVE));

  @Override
  public Flux<Customer> findAll() {
    return Flux.fromIterable(customers);
  }

  @Override
  public Flux<Customer> findPage(int page, int size) {
    return Flux.fromStream(customers.stream().skip((long) page * size).limit(size));
  }

  @Override
  public Mono<Customer> findById(String id) {
    return Mono.justOrEmpty(
        customers.stream().filter(customer -> customer.id().equals(id)).findFirst());
  }
}