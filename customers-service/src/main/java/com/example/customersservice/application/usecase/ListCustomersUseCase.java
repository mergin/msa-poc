package com.example.customersservice.application.usecase;

import com.example.customersservice.domain.model.Customer;
import com.example.customersservice.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ListCustomersUseCase {

  private final CustomerRepository customerRepository;

  public ListCustomersUseCase(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public Flux<Customer> execute(int page, int size) {
    int resolvedPage = Math.max(0, page);
    int resolvedSize = Math.max(1, Math.min(size, 100));
    return customerRepository.findPage(resolvedPage, resolvedSize);
  }
}
