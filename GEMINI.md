# Budget Buddy API - Gemini Context

This file provides context and instructions for AI agents working on the `budget-buddy-api` project.

## Project Overview

**Budget Buddy API** is a RESTful service for personal budget management. It's built as a multi-tenant, secure, and auditable backend for the Budget Buddy ecosystem.

- **Stack:** Java 25, Spring Boot 4.0.5, Spring Data JDBC, PostgreSQL, Liquibase, MapStruct, Lombok.
- **Architecture:** 
  - **API-First:** OpenAPI contracts are externalized in `budget-buddy-contracts` and consumed as a dependency.
  - **Generic CRUDL Framework:** Located in `base/crudl/`, providing common base classes for entities, repositories, services, and controllers.
  - **Security:** Stateless JWT for access tokens + DB-stored opaque refresh tokens.
- **Key Modules:**
  - `base/`: Core infrastructure, CRUDL framework, and global exception handling.
  - `user/`: User entity and service management.
  - `security/`: Auth controllers, JWT/Token services, and security configuration.
  - `category/`: Budget categories management.
  - `transaction/`: Financial transactions management.

## Building and Running

### Prerequisites
- Java 25
- Docker & Docker Compose (for PostgreSQL and Testcontainers)
- GitHub Personal Access Token (for accessing `budget-buddy-contracts` from GitHub Packages)

### Key Commands
```bash
# Run locally with 'dev' profile (auto-starts PostgreSQL via Spring Docker Compose)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Build project (requires GitHub credentials)
./gradlew build -Pgpr.user=YOUR_USERNAME -Pgpr.key=YOUR_TOKEN

# Testing
./gradlew test                  # Unit tests only
./gradlew integrationTest       # Integration tests (requires Docker)
./gradlew check                 # Runs all tests and quality checks

# Database Migrations
# Add new SQL migrations to src/main/resources/db/changelog/migrations/
```

## Development Conventions

### Coding Style
- **`var` keyword:** Use `var` for all local variable declarations where type is clear.
- **Lombok:** Extensively used for `@Data`, `@Value`, `@Builder`, and `@RequiredArgsConstructor`.
- **MapStruct:** Used for all DTO ↔ Entity mappings.
- **Exception Handling:** Centralized in `GlobalExceptionHandler`. Services should throw specific domain exceptions (e.g., `EntityNotFoundException`).

### Architecture Standards
- **CRUDL Pattern:** Extend `AbstractBaseEntityService` or `OwnableEntityService` for new domain services to inherit standard CRUDL logic and tenant isolation.
- **Multi-tenancy:** `OwnableEntity` and `OwnableEntityService` ensure that users only access their own data via `ownerId` filtering.
- **Auditability:** `AuditableEntity` automatically manages `createdAt` and `updatedAt` timestamps.

### Testing Practices
- **Framework:** JUnit 5, Mockito, and AssertJ.
- **Structure:** Use Given/When/Then comment blocks in all test methods.
- **Naming:** Follow the pattern `should_<action>_When_<condition>()`.
- **Nesting:** Group tests by scenario or method using `@Nested` classes.
- **Assertions:** Use `ArgumentCaptor` for complex side-effects and `assertThat(result).returns(...)` for multi-field verification.
- **Integration Tests:** Extend `BaseMvcIntegrationTest` for API-level tests or `BaseIntegrationTest` for service/repository layer tests.

## Adding a New Feature
1. Update `budget-buddy-contracts` if API changes are required.
2. Bump `budgetBuddyContractsVersion` in `build.gradle.kts`.
3. Create Liquibase migration if schema changes are needed.
4. Implement: Entity → Repository → Mapper → Service → Controller (extending CRUDL base classes).
5. Add Integration Tests in `src/integrationTest/`.
6. Increment semantic version in `gradle.properties`.
