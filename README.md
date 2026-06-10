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
- schema versioning discipline
- realistic database testing with Testcontainers

## Possible next improvements

- add Docker Compose for the full local runtime stack
- strengthen observability with Actuator and metrics
- document the core business rules more explicitly in the README
- add CI if the repository becomes a main portfolio project

## Author

Built as part of my Java backend training and kept as a portfolio project to showcase business-oriented backend development, with a particular interest in financial use cases, secure application design, and strong service-layer architecture.
