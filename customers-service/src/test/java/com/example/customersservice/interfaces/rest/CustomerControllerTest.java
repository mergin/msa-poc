package com.example.customersservice.interfaces.rest;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

import com.example.customersservice.application.usecase.GetCustomerByIdUseCase;
import com.example.customersservice.application.usecase.ListCustomersUseCase;
import com.example.customersservice.domain.model.Customer;
import com.example.customersservice.domain.model.CustomerStatus;
import com.example.customersservice.interfaces.rest.exception.ResourceNotFoundException;
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
  void shouldReturnCustomersList() throws Exception {
    when(listCustomersUseCase.execute(eq(0), eq(20)))
      .thenReturn(
        Flux.just(new Customer("c-001", "Alice Martínez", "alice@example.com", CustomerStatus.ACTIVE)));

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
          Flux.just(new Customer("c-011", "Customer 11", "customer11@example.com", CustomerStatus.ACTIVE)));

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
  void shouldReturnNotFoundForMissingCustomer() throws Exception {
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
