package com.example.customersservice.customer.application;

import com.example.customersservice.customer.api.exception.ResourceNotFoundException;
import com.example.customersservice.customer.domain.Customer;
import com.example.customersservice.customer.domain.CustomerRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetCustomerByIdUseCase {

  private final CustomerRepository customerRepository;

  public GetCustomerByIdUseCase(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public Mono<Customer> execute(String id) {
    return customerRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer not found")));
  }
}