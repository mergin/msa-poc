# Implementation Roadmap

Recommended execution order: **Phase 1 → Phase 2 → Phase 3 → Phase 4**.
Each phase builds on the previous one (Phase 2 protects the cross-service call added in Phase 1,
Phase 3 makes multi-hop traces visible, Phase 4 secures the edge).

---

## Phase 1 — Cross-service call: account owner enrichment

**Goal:** `GET /accounts/{id}/owner` returns the account enriched with its owner's customer data,
fetched reactively from `customers-service` via `WebClient`.

The `Account` domain model already carries `ownerId`, so no schema changes are needed.

### 1.1 — Add WebClient infrastructure to `accounts-service`

- [x] Add a `WebClient` bean to `AccountsServiceApplication` (or a dedicated `@Configuration` class):
  ```java
  @Bean
  public WebClient customersWebClient(
      @Value("${clients.customers.base-url:http://localhost:8081}") String baseUrl) {
    return WebClient.builder().baseUrl(baseUrl).build();
  }
  ```
- [x] Add the base-URL property to `accounts-service/src/main/resources/application.yml`:
  ```yaml
  clients:
    customers:
      base-url: ${CUSTOMERS_BASE_URL:http://localhost:8081}
  ```

### 1.2 — Create the `CustomerSummary` DTO in `accounts-service`

- [x] Create `accounts-service/src/main/java/com/example/accountsservice/infrastructure/client/dto/CustomerSummary.java`:
  ```java
  package com.example.accountsservice.infrastructure.client.dto;

  public record CustomerSummary(String id, String firstName, String lastName, String email) {}
  ```

### 1.3 — Create `CustomersClient`

- [x] Create `accounts-service/src/main/java/com/example/accountsservice/infrastructure/client/CustomersClient.java`:
  ```java
  package com.example.accountsservice.infrastructure.client;

  import com.example.accountsservice.infrastructure.client.dto.CustomerSummary;
  import org.springframework.stereotype.Component;
  import org.springframework.web.reactive.function.client.WebClient;
  import reactor.core.publisher.Mono;

  @Component
  public class CustomersClient {

    private final WebClient webClient;

    public CustomersClient(WebClient customersWebClient) {
      this.webClient = customersWebClient;
    }

    public Mono<CustomerSummary> findById(String customerId) {
      return webClient.get()
          .uri("/customers/{id}", customerId)
          .retrieve()
          .bodyToMono(CustomerSummary.class);
    }
  }
  ```

### 1.4 — Create the `AccountOwnerResponse` DTO

- [x] Create `accounts-service/src/main/java/com/example/accountsservice/interfaces/rest/dto/AccountOwnerResponse.java`:
  ```java
  package com.example.accountsservice.interfaces.rest.dto;

  import com.example.accountsservice.domain.model.Account;
  import com.example.accountsservice.infrastructure.client.dto.CustomerSummary;

  public record AccountOwnerResponse(
      String accountId,
      String accountNumber,
      String ownerId,
      String ownerFirstName,
      String ownerLastName,
      String ownerEmail) {

    public static AccountOwnerResponse of(Account account, CustomerSummary owner) {
      return new AccountOwnerResponse(
          account.id(),
          account.accountNumber(),
          owner.id(),
          owner.firstName(),
          owner.lastName(),
          owner.email());
    }
  }
  ```

### 1.5 — Create `GetAccountOwnerUseCase`

- [x] Create `accounts-service/src/main/java/com/example/accountsservice/application/usecase/GetAccountOwnerUseCase.java`:
  ```java
  package com.example.accountsservice.application.usecase;

  import com.example.accountsservice.domain.repository.AccountRepository;
  import com.example.accountsservice.infrastructure.client.CustomersClient;
  import com.example.accountsservice.interfaces.rest.dto.AccountOwnerResponse;
  import org.springframework.stereotype.Service;
  import reactor.core.publisher.Mono;

  @Service
  public class GetAccountOwnerUseCase {

    private final AccountRepository accountRepository;
    private final CustomersClient customersClient;

    public GetAccountOwnerUseCase(AccountRepository accountRepository,
                                  CustomersClient customersClient) {
      this.accountRepository = accountRepository;
      this.customersClient = customersClient;
    }

    public Mono<AccountOwnerResponse> execute(String accountId) {
      return accountRepository.findById(accountId)
          .flatMap(account -> customersClient.findById(account.ownerId())
              .map(owner -> AccountOwnerResponse.of(account, owner)));
    }
  }
  ```

### 1.6 — Add the endpoint to `AccountController`

- [x] Inject `GetAccountOwnerUseCase` into `AccountController`.
- [x] Add the handler method:
  ```java
  @GetMapping("/{id}/owner")
  public Mono<AccountOwnerResponse> getAccountOwner(@PathVariable String id) {
    return getAccountOwnerUseCase.execute(id);
  }
  ```

### 1.7 — Expose the route through the gateway

- [x] Verify `GET /v1/accounts/{id}/owner` is already covered by the existing
  `Path=/v1/accounts/**` rule in `gateway-service/src/main/resources/application.yml`.
  No change needed — confirm with a manual `curl` after startup.

### 1.8 — Write a unit test for `GetAccountOwnerUseCase`

- [x] Create `accounts-service/src/test/.../application/usecase/GetAccountOwnerUseCaseTest.java`
  mocking both `AccountRepository` and `CustomersClient` with Mockito + `StepVerifier`.

---

## Phase 2 — Resilience: circuit breaker and retry on the cross-service call

**Goal:** Protect `CustomersClient` with a Resilience4j reactive circuit breaker and a retry policy
so that a slow or unavailable `customers-service` degrades gracefully instead of cascading.

### 2.1 — Add Resilience4j dependency to `accounts-service`

- [x] Add to `accounts-service/pom.xml`:
  ```xml
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
  </dependency>
  ```
  `spring-cloud.version` is already managed in the root `pom.xml` (`2024.0.0`), so no version is needed.

### 2.2 — Configure the circuit breaker and retry instances

- [x] Add to `accounts-service/src/main/resources/application.yml`:
  ```yaml
  resilience4j:
    circuitbreaker:
      instances:
        customersClient:
          sliding-window-size: 10
          failure-rate-threshold: 50
          wait-duration-in-open-state: 10s
          permitted-number-of-calls-in-half-open-state: 3
          register-health-indicator: true
    retry:
      instances:
        customersClient:
          max-attempts: 3
          wait-duration: 200ms
          retry-exceptions:
            - org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable
            - java.net.ConnectException
  ```

### 2.3 — Apply circuit breaker and retry in `CustomersClient`

- [x] Inject `ReactiveCircuitBreakerFactory` into `CustomersClient`.
- [x] Wrap the `WebClient` call:
  ```java
  import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
  import reactor.util.retry.Retry;
  import java.time.Duration;

  // In findById:
  return webClient.get()
      .uri("/customers/{id}", customerId)
      .retrieve()
      .bodyToMono(CustomerSummary.class)
      .retryWhen(Retry.backoff(2, Duration.ofMillis(200))
          .filter(ex -> ex instanceof ConnectException))
      .transform(it ->
          circuitBreakerFactory.create("customersClient").run(it, throwable -> Mono.empty()));
  ```
- [x] Update `GetAccountOwnerUseCase` to handle an empty `Mono` from the fallback
  (e.g., return `Mono.error(new CustomerServiceUnavailableException(...))`  or a partial response).

### 2.4 — Expose Resilience4j health and metrics via Actuator

- [x] Add to `accounts-service/src/main/resources/application.yml`:
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,info,circuitbreakers,retries
    health:
      circuitbreakers:
        enabled: true
  ```

### 2.5 — Write an integration test for the circuit breaker fallback

- [ ] Use `WireMock` (or `MockWebServer` from OkHttp) to simulate a failing `customers-service`
  and assert that the circuit opens after the configured threshold and that the fallback is invoked.
- [ ] Add test dependency to `accounts-service/pom.xml`:
  ```xml
  <dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.5.4</version>
    <scope>test</scope>
  </dependency>
  ```

---

## Phase 3 — Observability: distributed tracing with Micrometer + Zipkin

**Goal:** Every request that crosses gateway → customers/accounts produces correlated spans
visible in a local Zipkin UI.

### 3.1 — Add Zipkin to `docker-compose.yml`

- [x] Add the Zipkin service:
  ```yaml
  zipkin:
    image: openzipkin/zipkin:3
    ports:
      - "9411:9411"
  ```

### 3.2 — Add tracing dependencies to all three services

- [x] Add to each `pom.xml` (`customers-service`, `accounts-service`, `gateway-service`):
  ```xml
  <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
  </dependency>
  <dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
  </dependency>
  ```
  Both artifacts are version-managed by `spring-boot-dependencies 3.4.3`.

### 3.3 — Configure tracing in each service's `application.yml`

- [x] Add to `customers-service/src/main/resources/application.yml`:
  ```yaml
  management:
    tracing:
      sampling:
        probability: 1.0
  spring:
    zipkin:
      base-url: ${ZIPKIN_BASE_URL:http://localhost:9411}
  ```
- [x] Repeat the same block for `accounts-service/src/main/resources/application.yml`.
- [x] Repeat the same block for `gateway-service/src/main/resources/application.yml`.

### 3.4 — Verify trace propagation

- [ ] Start all services: `docker compose up -d`, then start all three Spring Boot apps.
- [ ] Send a request: `curl http://localhost:8080/v1/accounts/{id}/owner`
- [ ] Open Zipkin at `http://localhost:9411` and confirm a trace with ≥ 3 spans
  (gateway → accounts-service → customers-service).

### 3.5 — Expose tracing metadata in Actuator

- [x] Add `metrics` to the Actuator `include` list in each service's `application.yml`
  so `GET /actuator/metrics/http.server.requests` surfaces latency per endpoint.

---

## Phase 4 — Security: JWT authentication at the gateway

**Goal:** All `GET /v1/**` routes require a valid signed JWT. No external IdP is needed —
use a shared HMAC-SHA256 secret for the PoC.

### 4.1 — Add Security + OAuth2 Resource Server to `gateway-service`

- [x] Add to `gateway-service/pom.xml`:
  ```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
  </dependency>
  ```

### 4.2 — Configure the shared secret in `gateway-service/src/main/resources/application.yml`

- [x] Add:
  ```yaml
  spring:
    security:
      oauth2:
        resourceserver:
          jwt:
            # 256-bit Base64-encoded secret — override via env var in any real environment
            secret: ${JWT_SECRET:bXNhLXBvYy1zZWNyZXQta2V5LWF0LWxlYXN0LTI1Ni1iaXRz}
  ```

### 4.3 — Create `SecurityConfig` in `gateway-service`

- [x] Create `gateway-service/src/main/java/com/example/gatewayservice/SecurityConfig.java`:
  ```java
  package com.example.gatewayservice;

  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
  import org.springframework.security.config.web.server.ServerHttpSecurity;
  import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
  import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
  import org.springframework.security.web.server.SecurityWebFilterChain;

  import javax.crypto.spec.SecretKeySpec;
  import java.util.Base64;

  @Configuration
  @EnableWebFluxSecurity
  public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
      return http
          .csrf(ServerHttpSecurity.CsrfSpec::disable)
          .authorizeExchange(ex -> ex
              .pathMatchers("/actuator/**").permitAll()
              .anyExchange().authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
          .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
        @Value("${spring.security.oauth2.resourceserver.jwt.secret}") String secret) {
      byte[] keyBytes = Base64.getDecoder().decode(secret);
      SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
      return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }
  }
  ```

### 4.4 — Generate a test JWT for local development

- [x] Add a README section (or a shell script `scripts/generate-test-jwt.py`) documenting
  how to mint a token using the shared secret, e.g., via the [jwt.io](https://jwt.io) debugger
  or a small Java/Python snippet:
  ```python
  # pip install pyjwt
  import jwt, base64, datetime
  secret = base64.b64decode("bXNhLXBvYy1zZWNyZXQta2V5LWF0LWxlYXN0LTI1Ni1iaXRz")
  token = jwt.encode(
      {"sub": "dev-user", "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=8)},
      secret, algorithm="HS256")
  print(token)
  ```
- [ ] Confirm a request with the token succeeds:
  ```bash
  curl -H "Authorization: Bearer <token>" http://localhost:8080/v1/accounts
  ```
- [ ] Confirm a request without a token returns `401`.

### 4.5 — Write a `@WebFluxTest` for `SecurityConfig`

- [ ] Add a test that mocks the downstream routes and asserts:
  - `GET /v1/accounts` without a token → `401 Unauthorized`.
  - `GET /v1/accounts` with a valid token → routed (not `401`/`403`).
  - `GET /actuator/health` without a token → `200 OK`.
