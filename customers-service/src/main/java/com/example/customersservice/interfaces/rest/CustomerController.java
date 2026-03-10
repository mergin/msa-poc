package com.example.customersservice.interfaces.rest;

import com.example.customersservice.application.usecase.GetCustomerByIdUseCase;
import com.example.customersservice.application.usecase.ListCustomersUseCase;
import com.example.customersservice.interfaces.rest.dto.CustomerResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/customers")
public class CustomerController {

  private final ListCustomersUseCase listCustomersUseCase;
  private final GetCustomerByIdUseCase getCustomerByIdUseCase;

  public CustomerController(
      ListCustomersUseCase listCustomersUseCase, GetCustomerByIdUseCase getCustomerByIdUseCase) {
    this.listCustomersUseCase = listCustomersUseCase;
    this.getCustomerByIdUseCase = getCustomerByIdUseCase;
  }

  @GetMapping
  public Flux<CustomerResponse> getCustomers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return listCustomersUseCase.execute(page, size).map(CustomerResponse::fromDomain);
  }

  @GetMapping("/{id}")
  public Mono<CustomerResponse> getCustomerById(@PathVariable String id) {
    return getCustomerByIdUseCase.execute(id).map(CustomerResponse::fromDomain);
  }
}
