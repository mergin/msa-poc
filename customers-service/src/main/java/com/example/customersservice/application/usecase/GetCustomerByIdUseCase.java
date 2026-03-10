package com.example.customersservice.application.usecase;

import com.example.customersservice.domain.model.Customer;
import com.example.customersservice.domain.repository.CustomerRepository;
import com.example.customersservice.interfaces.rest.exception.ResourceNotFoundException;
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
