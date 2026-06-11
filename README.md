# PayMyBuddy Fintech Backend

Spring Boot server-side web application for user management, account handling, peer-to-peer transfers, and bank deposit/withdrawal operations.

This project showcases a business-oriented Java backend with Spring MVC, Thymeleaf, Spring Security, JPA/Hibernate, MySQL, Flyway, DTO mapping with MapStruct, and integration testing with Testcontainers.

## Why this project stands out

This repository demonstrates:

- a clean layered Spring Boot architecture
- a business-focused financial domain
- server-side rendering with Spring MVC and Thymeleaf
- authentication and protected user flows with Spring Security
- relational persistence with MySQL and schema versioning with Flyway
- DTO-driven web layer with MapStruct
- integration tests backed by ephemeral MySQL containers through Testcontainers

## Functional scope

The application covers the following use cases:

- user registration and login
- account creation and account listing
- user-to-user connections
- peer-to-peer transfers between users
- bank deposit and withdrawal operations
- account activity history visualization

## Application flow

```text
Browser -> Spring MVC Controllers -> Services -> Repositories -> JPA/Hibernate -> MySQL
                                               -> Thymeleaf views
```

This is a server-side rendered application: controllers prepare the data, services apply business rules, repositories access the database, and Thymeleaf renders the final HTML views.

## Main business domains

### Users

The application manages user registration, authentication, and profile retrieval.

### Accounts

Each user can own one or more accounts used for transaction and bank transfer flows.

### Connections

Users can add trusted connections ("buddies") and use them as recipients in peer-to-peer transfers.

### Transactions

The application supports money transfers between user accounts with business validation.

### Bank transfers

The application also supports deposit and withdrawal operations between an account and the bank side.

## Architecture

The codebase follows a layered structure:

- `model`
  JPA entities representing users, accounts, transfers, transactions, and connections
- `repository`
  Spring Data JPA repositories
- `service`
  business logic and transaction orchestration
- `web/controller`
  MVC controllers handling pages and form submissions
- `web/dto`
  DTOs used by the web layer
- `web/mapper`
  mappings between DTOs and entities using MapStruct
- `security`
  authentication and access control configuration
- `infra`
  startup and demo data initialization

## Security

The application uses Spring Security with a classic server-side login flow.

It protects business pages and keeps authentication integrated directly in the MVC application.

Security responsibilities include:

- access protection for authenticated pages
- custom login page handling
- password encoding with BCrypt

## Business rules and hardening decisions

This project goes beyond CRUD. It includes a progressive hardening effort inspired by real financial constraints: authorization, accounting consistency, operational robustness, and auditability.

### Authorization and transfer eligibility

Peer-to-peer transfers are not accepted blindly.

The backend now enforces the following business rules:

- a user can only initiate a transfer from an account they actually own
- a recipient must be an authorized connection ("buddy")
- sender and receiver accounts must use the same currency

These checks reduce classic business risks such as IDOR-style misuse, accidental transfer routing, or uncontrolled cross-currency behavior.

### Balance integrity and concurrency

Financial code must behave correctly under concurrent access, not only in a single-user happy path.

To protect balances, the project now includes:

- pessimistic locking on balance updates
- deterministic lock ordering for transfer-related account locking
- targeted tests documenting lost update and consistency risks

This is important because a financial application must preserve balance correctness even when multiple operations hit the same accounts at nearly the same time.

### Fees and accounting consistency

Transfer fees are not discarded. They are credited to a dedicated system fees account.

This means:

- the sender is debited by `amount + fee`
- the recipient receives `amount`
- the fee is credited to a dedicated PayMyBuddy system account
- the total sum of balances remains conserved

This is a stronger accounting choice than letting fees "disappear", because it keeps the system financially explainable.

### Ledger direction

The project also includes a first `ledger_entries` foundation.

The current application still updates account balances directly, but the schema now prepares a more explicit accounting direction where:

- each movement can be represented as append-only debit/credit entries
- balances become derivable from the ledger
- reconciliation and audit become easier over time

This is intentionally aligned with financial software practices, where traceability matters as much as functionality.

### Regulatory-style transfer limits

Several controls were added to reflect real-world payment and AML thinking:

- maximum amount per transaction
- daily transfer count limit
- daily cumulative transfer amount limit

These controls are configurable and enforced in the service layer, which makes them explicit, testable, and easy to evolve.

### Idempotency and operational robustness

The transfer flow now includes an idempotency key.

This prevents a double submit from creating duplicate transfers in cases such as:

- double click on a payment button
- browser retry
- accidental repeated POST

In a payment context, this kind of safeguard is operationally important because duplicate execution is often more damaging than a simple validation failure.

### Observability and audit trail

The application now exposes controlled operational endpoints through Spring Boot Actuator:

- `health`
- `metrics`

Sensitive actuator access remains protected, while health stays available for basic supervision.

The project also persists an immutable financial operation audit log for successful monetary operations. Each audit record captures a business trace such as:

- operation type
- source entity
- actor user
- impacted account
- counterparty account when relevant
- amount, fee, currency
- business description
- timestamp

This improves explainability, post-incident analysis, and long-term audit readiness.

## Persistence

### MySQL

MySQL is used because the domain is strongly relational:

- users
- accounts
- transfers
- connections
- transaction history

This fits well with a structured financial domain where consistency and explicit schema evolution matter.

### Flyway

The relational schema is versioned with Flyway, which ensures controlled database evolution and avoids schema drift.

### Accounting direction

The project now includes a dedicated system fees account and a first ledger foundation through the `ledger_entries` table.

The current implementation still updates account balances directly, but the ledger schema prepares a stronger accounting model where:

- each financial operation can be decomposed into explicit debit and credit entries
- balances become derivable from entries instead of being only stored state
- fee collection can be traced as a proper accounting movement

This is especially relevant for financial software because it improves auditability, explainability, and long-term consistency.

## Testing

The project includes:

- unit tests on service logic
- integration tests using Testcontainers with ephemeral MySQL

This is a strong point of the repository because it validates persistence behavior in a realistic environment instead of relying only on mocks.

## Tech stack

- Java 21
- Spring Boot 3.5
- Spring MVC
- Thymeleaf
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- Flyway
- Lombok
- MapStruct
- Testcontainers
- Maven

## Local run

### Prerequisites

- Java 21
- Maven 3.9+
- Docker running locally for integration tests
- MySQL locally, or MySQL in Docker

### Application startup

```bash
mvn spring-boot:run
```

Default local URL:

- `http://localhost:8080`

### Database configuration

The application expects a MySQL database:

- URL: `jdbc:mysql://127.0.0.1:3306/paymybuddy`
- username: `paymybuddy` by default
- password: `paymybuddy` by default

These values can be overridden with environment variables:

- `DB_USERNAME`
- `DB_PASSWORD`

### Demo profile

A demo profile is available to preload sample users and accounts:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

## Tests

Run all tests:

```bash
mvn test
```

Run full verification:

```bash
mvn clean verify
```

Integration tests start an ephemeral MySQL container automatically through Testcontainers.

## Repository structure

```text
src/main/java/com/alcaniz/paymybuddy/
  infra/
  model/
  repository/
  security/
  service/
  web/

src/main/resources/
  db/migration/
  templates/
```

## What this project demonstrates

Beyond simple CRUD, this project demonstrates:

- business-oriented backend design
- financial-domain modeling
- transactional service logic
- secure MVC application design
- transfer hardening with explicit business controls
- accounting consistency and fee traceability
- immutable financial audit logging
- operational visibility with health and metrics endpoints
- schema versioning discipline
- realistic database testing with Testcontainers

## Possible next improvements

- add Docker Compose for the full local runtime stack
- add CI if the repository becomes a main portfolio project

## Author

Built as part of my Java backend training and kept as a portfolio project to showcase business-oriented backend development, with a particular interest in financial use cases, secure application design, and strong service-layer architecture.
