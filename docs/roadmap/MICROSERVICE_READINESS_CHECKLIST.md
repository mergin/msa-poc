# Microservice Readiness Checklist

Assessment date: 2026-03-10

## Current status

- ✅ **Service decomposition (business capabilities split):** `customers-service` + `accounts-service` + edge `gateway-service`.
- ✅ **Independent runtime processes/ports:** gateway `8080`, customers `8081`, accounts `8082`.
- ✅ **API Gateway routing and CORS at edge:** `/v1/customers/**` and `/v1/accounts/**`.
- ✅ **Clear bounded-context internals:** feature-first packaging in `customers-service`, `accounts-service`, and `transactions-service`.
- ✅ **Data ownership/persistence:** PostgreSQL per microservice using reactive R2DBC adapters.
- ✅ **Reactive implementation:** business services are reactive end-to-end (`Flux`/`Mono`).
- ❌ **Service discovery / dynamic routing:** no Eureka/Consul or equivalent found.
- ❌ **Centralized config management:** no Spring Cloud Config setup found.
- ❌ **Resilience patterns:** no circuit breaker/retry/bulkhead dependencies found.
- ❌ **Security between services and edge auth:** no Spring Security/OAuth2 setup found.
- ⚠️ **Observability:** basic Actuator present, but no distributed tracing/metrics backend integration.

## Evidence

- Root modules and architecture description: `pom.xml`, `README.md`
- Gateway routes and exposed management endpoints: `gateway-service/src/main/resources/application.yml`
- Service ports and management endpoints:
  - `customers-service/src/main/resources/application.yml`
  - `accounts-service/src/main/resources/application.yml`
- PostgreSQL runtime for PoC:
  - `platform/docker/docker-compose.yml`
- Reactive PostgreSQL adapters:
  - `customers-service/src/main/java/com/example/customersservice/customer/infrastructure/persistence/PostgresCustomerRepository.java`
  - `accounts-service/src/main/java/com/example/accountsservice/account/infrastructure/persistence/PostgresAccountRepository.java`
  - `transactions-service/src/main/java/com/example/transactionsservice/transaction/infrastructure/persistence/PostgresTransactionRepository.java`
  - `transactions-service/src/main/java/com/example/transactionsservice/analytics/infrastructure/persistence/PostgresTransactionAnalyticsRepository.java`
- SQL initialization scripts:
  - `customers-service/src/main/resources/db/schema.sql`
  - `customers-service/src/main/resources/db/data.sql`
  - `accounts-service/src/main/resources/db/schema.sql`
  - `accounts-service/src/main/resources/db/data.sql`
  - `transactions-service/src/main/resources/db/schema.sql`
  - `transactions-service/src/main/resources/db/data.sql`
- Gateway dependencies (Gateway + Actuator): `gateway-service/pom.xml`
- Dependency scan for operational capabilities in all module `pom.xml` files:
  - no matches for `eureka|consul|configserver|spring-cloud-starter-config|resilience4j|circuitbreaker|spring-boot-starter-security|oauth2|zipkin|sleuth|micrometer-tracing|prometheus|springdoc`

## Conclusion

This repository follows a **reactive microservice-style architecture suitable for a PoC** with PostgreSQL-backed persistence. To be production-ready, it still needs core operational capabilities (service discovery/config, resilience, security, and deeper observability).
