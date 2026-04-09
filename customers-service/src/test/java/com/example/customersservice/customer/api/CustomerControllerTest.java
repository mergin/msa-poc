package com.example.customersservice.customer.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.customersservice.customer.api.exception.ResourceNotFoundException;
import com.example.customersservice.customer.application.GetCustomerByIdUseCase;
import com.example.customersservice.customer.application.ListCustomersUseCase;
import com.example.customersservice.customer.domain.Customer;
import com.example.customersservice.customer.domain.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(CustomerController.class)
@Import(ApiExceptionHandler.class)
class CustomerControllerTest {

  @Autowired private WebTestClient webTestClient;

  @MockBean private ListCustomersUseCase listCustomersUseCase;
  @MockBean private GetCustomerByIdUseCase getCustomerByIdUseCase;

  @Test
  void shouldReturnCustomersList() {
    when(listCustomersUseCase.execute(eq(0), eq(20)))
        .thenReturn(
            Flux.just(
                new Customer("c-001", "Alice Martínez", "alice@example.com", CustomerStatus.ACTIVE)));

    webTestClient
        .get()
        .uri("/customers")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].id")
        .isEqualTo("c-001")
        .jsonPath("$[0].status")
        .isEqualTo("active");
  }

  @Test
  void shouldReturnCustomersListWithPaginationParams() {
    when(listCustomersUseCase.execute(eq(1), eq(10)))
        .thenReturn(
            Flux.just(
                new Customer("c-011", "Customer 11", "customer11@example.com", CustomerStatus.ACTIVE)));

    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/customers").queryParam("page", 1).queryParam("size", 10).build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].id")
        .isEqualTo("c-011");
  }

  @Test
  void shouldReturnNotFoundForMissingCustomer() {
    when(getCustomerByIdUseCase.execute("missing"))
        .thenReturn(Mono.error(new ResourceNotFoundException("Customer not found")));

    webTestClient
        .get()
        .uri("/customers/missing")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.message")
        .isEqualTo("Customer not found");
  }
}