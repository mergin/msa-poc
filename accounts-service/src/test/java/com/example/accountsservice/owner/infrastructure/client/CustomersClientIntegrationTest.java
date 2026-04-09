package com.example.accountsservice.owner.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class CustomersClientIntegrationTest {

  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  private CustomersClient customersClient;

  @BeforeEach
  void setUp() {
    WebClient webClient = WebClient.builder().baseUrl(wireMock.baseUrl()).build();
    ReactiveResilience4JCircuitBreakerFactory factory =
        new ReactiveResilience4JCircuitBreakerFactory();
    customersClient = new CustomersClient(webClient, factory);
  }

  @Test
  void shouldReturnCustomerSummaryWhenDownstreamResponds() {
    wireMock.stubFor(
        get(urlPathEqualTo("/customers/c-1"))
            .willReturn(
                okJson(
                    """
                    {"id":"c-1","firstName":"Alice","lastName":"Smith","email":"alice@example.com"}
                    """)));

    StepVerifier.create(customersClient.findById("c-1"))
        .assertNext(
            cs -> {
              assertThat(cs.id()).isEqualTo("c-1");
              assertThat(cs.firstName()).isEqualTo("Alice");
              assertThat(cs.lastName()).isEqualTo("Smith");
              assertThat(cs.email()).isEqualTo("alice@example.com");
            })
        .verifyComplete();
  }

  @Test
  void shouldReturnEmptyMonoWhenDownstreamReturnsServerError() {
    wireMock.stubFor(get(urlPathEqualTo("/customers/c-1")).willReturn(serverError()));

    StepVerifier.create(customersClient.findById("c-1")).verifyComplete();
  }

  @Test
  void shouldReturnEmptyMonoWhenDownstreamReturns404() {
    wireMock.stubFor(
        get(urlPathEqualTo("/customers/c-1"))
            .willReturn(com.github.tomakehurst.wiremock.client.WireMock.notFound()));

    StepVerifier.create(customersClient.findById("c-1")).verifyComplete();
  }
}