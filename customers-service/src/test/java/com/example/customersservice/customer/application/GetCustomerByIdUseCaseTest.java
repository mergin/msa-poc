package com.example.customersservice.customer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.customersservice.customer.api.exception.ResourceNotFoundException;
import com.example.customersservice.customer.domain.Customer;
import com.example.customersservice.customer.domain.CustomerRepository;
import com.example.customersservice.customer.domain.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GetCustomerByIdUseCaseTest {

  private final CustomerRepository repository = Mockito.mock(CustomerRepository.class);
  private final GetCustomerByIdUseCase useCase = new GetCustomerByIdUseCase(repository);

  @Test
  void shouldReturnCustomerWhenFound() {
    Customer customer = new Customer("c-1", "Test", "test@example.com", CustomerStatus.ACTIVE);
    when(repository.findById("c-1")).thenReturn(Mono.just(customer));

    StepVerifier.create(useCase.execute("c-1"))
        .assertNext(result -> assertThat(result.id()).isEqualTo("c-1"))
        .verifyComplete();
  }

  @Test
  void shouldThrowNotFoundWhenCustomerDoesNotExist() {
    when(repository.findById("missing")).thenReturn(Mono.empty());

    StepVerifier.create(useCase.execute("missing"))
        .expectErrorMatches(
            error ->
                error instanceof ResourceNotFoundException
                    && "Customer not found".equals(error.getMessage()))
        .verify();
  }
}