package com.example.customersservice.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.customersservice.domain.model.Customer;
import com.example.customersservice.domain.model.CustomerStatus;
import com.example.customersservice.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class ListCustomersUseCaseTest {

  private final CustomerRepository repository = Mockito.mock(CustomerRepository.class);
  private final ListCustomersUseCase useCase = new ListCustomersUseCase(repository);

  @Test
  void shouldReturnAllCustomers() {
    when(repository.findPage(0, 20))
        .thenReturn(Flux.just(new Customer("c-1", "Test", "test@example.com", CustomerStatus.ACTIVE)));

    StepVerifier.create(useCase.execute(0, 20))
        .assertNext(customer -> assertThat(customer.id()).isEqualTo("c-1"))
        .verifyComplete();
  }
}
