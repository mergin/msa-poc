# Implementation Instructions

## Objective covered

- `customers-service` and `accounts-service` are reactive end-to-end (`Flux`/`Mono`).
- Both microservices use SQL persistence with PostgreSQL for PoC.
- In-memory repositories remain available only as an optional `in-memory` profile fallback.

## What was implemented

- Switched both business services to reactive stack (`spring-boot-starter-webflux`).
- Added reactive SQL stack:
  - `spring-boot-starter-data-r2dbc`
  - `r2dbc-postgresql`
- Added PostgreSQL adapters:
  - `PostgresCustomerRepository`
  - `PostgresAccountRepository`
- Added SQL initialization per service:
  - `schema.sql`
  - `data.sql`
  - 100 seeded rows per service for pagination scenarios
- Added local PostgreSQL runtime via root `docker-compose.yml` with one DB per service.

## Runtime configuration

- Customers DB (default): `r2dbc:postgresql://localhost:5433/customersdb`
- Accounts DB (default): `r2dbc:postgresql://localhost:5434/accountsdb`
- Credentials default to `msa` / `msa`.

Environment variables supported:

- Customers: `CUSTOMERS_R2DBC_URL`, `CUSTOMERS_DB_USER`, `CUSTOMERS_DB_PASSWORD`
- Accounts: `ACCOUNTS_R2DBC_URL`, `ACCOUNTS_DB_USER`, `ACCOUNTS_DB_PASSWORD`

## Run order

1. `docker compose up -d`
2. `mvn -pl customers-service spring-boot:run`
3. `mvn -pl accounts-service spring-boot:run`
4. `mvn -pl gateway-service spring-boot:run`

## Notes

- `in-memory` profile can be enabled for either service when DB is unavailable.
- API contract remains unchanged.
- List endpoints support pagination with `page` and `size` query params (defaults `0` and `20`, max size `100`).

## Acceptance checklist

- ✅ The 2 microservices are implemented as reactive services following WebFlux patterns.
- ✅ The 2 microservices use SQL persistence with PostgreSQL for PoC purposes.
- ✅ README and instruction documents describe the reactive + PostgreSQL setup.
- ⚠️ End-to-end test execution requires Maven installed on the local environment.
