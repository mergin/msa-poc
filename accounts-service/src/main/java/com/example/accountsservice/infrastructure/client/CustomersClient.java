package com.example.accountsservice.infrastructure.client;

import com.example.accountsservice.infrastructure.client.dto.CustomerSummary;
import java.net.ConnectException;
import java.time.Duration;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class CustomersClient {

  private final WebClient webClient;
  private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

  public CustomersClient(
      WebClient customersWebClient,
      ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory) {
    this.webClient = customersWebClient;
    this.circuitBreakerFactory = circuitBreakerFactory;
  }

  public Mono<CustomerSummary> findById(String customerId) {
    Mono<CustomerSummary> call =
        webClient
            .get()
            .uri("/customers/{id}", customerId)
            .retrieve()
            .bodyToMono(CustomerSummary.class)
            .retryWhen(
                Retry.backoff(2, Duration.ofMillis(200))
                    .filter(ex -> ex instanceof ConnectException));

    return circuitBreakerFactory
        .create("customersClient")
        .run(call, throwable -> Mono.empty());
  }
}
