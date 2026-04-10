# Shared Documentation for Budget Buddy API

This file contains common guidance shared across all AI agent instruction files (CLAUDE.md, GEMINI.md, copilot-instructions.md).

**Agent-specific files should reference this document for common content and only override with agent-specific customizations.**

---

## Technology Stack

- **Java 25** — with `var` for local variable declarations
- **Spring Boot 4.0.5**
- **Spring Data JDBC** — lightweight persistence (not JPA)
- **PostgreSQL** — production database
- **Liquibase** — database migrations
- **MapStruct** — Entity ↔ DTO mapping
- **Lombok** — boilerplate reduction

---

## Architecture

### API-First Design

The OpenAPI spec lives in the external [budget-buddy-contracts](https://github.com/budget-buddy-org/budget-buddy-contracts) repository. Models and interfaces are consumed as the `com.budgetbuddy:budget-buddy-contracts` dependency (version in `build.gradle.kts`). To change the API contract:
1. Update the `budget-buddy-contracts` repository
2. Bump `budgetBuddyContractsVersion` in `build.gradle.kts`
3. Run `./gradlew build` to fetch new contracts
4. Implement the business logic

### Generic CRUDL Framework

Located in `base/crudl/`, this framework is the backbone of all domain features:

- **`BaseEntity`** — common entity interface; extended by:
  - **`AuditableEntity`** — auto-managed `createdAt`/`updatedAt` via `BaseEntityListener`
  - **`OwnableEntity`** — adds `ownerId` for multi-tenant isolation
- **`BaseEntityRepository`** — extends Spring Data JDBC interfaces
- **`AbstractBaseEntityService`** → **`OwnableEntityService`** (auto-filters all queries by `ownerId`) → domain services
- **`BaseEntityController`** — delegates to five internal methods: `createInternal()`, `readInternal()`, `updateInternal()`, `deleteInternal()`, `listInternal()`; always override `createdURI()`
- **`BaseEntityMapper`** (MapStruct) — entity ↔ DTO conversion
- **`BaseEntityValidator`** — custom validators autowired as a `Set` into services

### Package Layout

```
base/              # infrastructure: config, exception handling, CRUDL framework
user/              # user management
security/          # auth endpoints, JWT generation/parsing, refresh token management
category/          # category CRUDL
transaction/       # transaction CRUDL
```

### Security Model

- **Stateless JWT architecture:** Access tokens contain `ownerId` claim for multi-tenant filtering
- **Refresh tokens:** Opaque tokens stored in DB (enables revocation)
- **Public endpoints:** `POST /v1/auth/**` and `GET /actuator/health`
- **Protected endpoints:** All others require `Authorization: Bearer <access_token>` header
- **Multi-tenancy:** `OwnableEntityService` automatically filters all queries by `ownerId`

### Database

- **ORM:** Spring Data JDBC (not JPA; simpler, no N+1 queries)
- **Schema management:** Liquibase at `src/main/resources/db/changelog/`
  - Master changelog: `db.changelog-master.yaml`
  - Migrations: numbered SQL files in `migrations/` folder
  - Migrations auto-detected at startup; never edit existing migrations

### Error Handling

- `EntityNotFoundException` — thrown by services when entity not found
- Central error handling in `base/` exception handlers
- **Never** catch exceptions in controllers — let them bubble up for centralized handling

---

## Build & Test Commands

### Running Locally

```bash
# Run with dev profile (auto-starts PostgreSQL via Docker Compose)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Testing

```bash
./gradlew test                  # Unit tests only
./gradlew integrationTest       # Integration tests (Testcontainers + PostgreSQL)
./gradlew check                 # All tests + quality checks

# Run a single test class
./gradlew test --tests "com.budget.buddy.budget_buddy_api.CategoryServiceTest"
./gradlew integrationTest --tests "com.budget.buddy.budget_buddy_api.CategoryControllerIT"
```

### Building

```bash
./gradlew build                 # Build JAR
./gradlew bootBuildImage --imageName=ghcr.io/your-username/budget-buddy-api:latest
```

---

## Testing Conventions

### Test Structure

- **Unit tests:** `src/test/java/` — plain Mockito, no base class required
- **Integration tests:** `src/integrationTest/java/` — extend `BaseMvcIntegrationTest` (HTTP layer) or `BaseIntegrationTest` (repository/service layer)

### Test Naming & Organization

- Test method naming: `should_<action>_When_<condition>()`
- Use `// Given`, `// When`, `// Then` comment sections in every test
- Group tests by method or scenario using `@Nested` classes

### Assertions & Matchers

- Use `ArgumentCaptor` instead of vague `any()` matchers
- Multi-field assertions: `assertThat(result).returns(expected, Type::accessor)`
- Framework: JUnit 5, Mockito, AssertJ

### Code Style in Tests

- Use `var` for all local variable declarations
- Example:
  ```java
  // Given
  var user = createTestUser("admin");
  
  // When
  var result = userService.findById(user.getId());
  
  // Then
  assertThat(result)
    .isPresent()
    .returns("admin", User::getUsername);
  ```

---

## Adding a New Feature

1. **If API contract changes:** Update `budget-buddy-contracts` repo, bump `budgetBuddyContractsVersion` in `build.gradle.kts`
2. **Add Liquibase migration** if schema changes needed (numbered SQL files in `src/main/resources/db/changelog/migrations/`)
3. **Implement in order:** Entity → Repository → Mapper → Service → Controller
4. **Add integration tests** in `src/integrationTest/java/`
5. **Increment version** in `gradle.properties` (use semantic versioning)
6. **Run full test suite:** `./gradlew check`

---

## Versioning

- **Semantic versioning:** `MAJOR.MINOR.PATCH` in `gradle.properties`
- Increment **before** every merge to `main` for any code or configuration changes

---

## Code Conventions

### Variable Naming

- Use `var` for all local variable declarations (language feature, not just style)

### Dependency Injection

- Constructor injection only; fields are `final`
- Autowiring of validators and services via constructor

### Mappers

- MapStruct for Entity ↔ DTO conversions
- Entity models (JDBC) kept separate from API models (OpenAPI-generated)

### Validators

- Custom validators implement `Validator` interface
- Autowired as `Set<Validator>` into services
- Each validator responsible for one domain rule

### Lombok Usage

- `@Getter`, `@Setter`, `@Data`, `@Builder` for reducing boilerplate
- MapStruct processors configured to work with Lombok
