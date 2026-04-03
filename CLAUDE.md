# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Java 25, Spring Boot 4.0.4, Spring Data JDBC, PostgreSQL, Liquibase, MapStruct, Lombok, OpenAPI Generator.

## Commands

```bash
# Generate code from OpenAPI spec (required after spec changes, before building)
./gradlew openApiGenerate

# Run locally (auto-starts PostgreSQL via Docker Compose)
./gradlew bootRun --args='--spring.profiles.active=dev'

./gradlew build
./gradlew test                  # unit tests only
./gradlew integrationTest       # Testcontainers + real PostgreSQL
./gradlew check                 # all tests

# Run a single test class
./gradlew test --tests "com.budget.buddy.budget_buddy_api.SomeTest"
./gradlew integrationTest --tests "com.budget.buddy.budget_buddy_api.SomeIT"
```

## Architecture

**API-First**: `src/main/resources/openapi.yaml` is the source of truth. Update the spec before implementing endpoints, then run `openApiGenerate` to regenerate models and interfaces.

**Generic CRUDL framework** lives in `base/crudl/` and is the backbone of all domain features:

- `BaseEntity` — common entity interface; extended by:
  - `AuditableEntity` — auto-managed `createdAt`/`updatedAt` via `BaseEntityListener`
  - `OwnableEntity` — adds `ownerId` for multi-tenant isolation
- `BaseEntityRepository` — extends Spring Data JDBC interfaces
- `AbstractBaseEntityService` → `OwnableEntityService` (auto-filters all queries by `ownerId`) → domain services (e.g. `CategoryService`)
- `BaseEntityController` — domain controllers implement the generated OpenAPI interface and extend this; delegate to `createInternal()`, `readInternal()`, `updateInternal()`, `deleteInternal()`, `listInternal()`; always override `createdURI()`
- `BaseEntityMapper` (MapStruct) — entity ↔ DTO conversion
- `BaseEntityValidator` — custom validators autowired as a `Set` into services

**Package layout**:
```
base/           # infrastructure: config, exception handling, crudl framework
user/           # user management
security/       # auth endpoints, JWT generation/parsing, refresh token management
category/       # category CRUDL
transaction/    # transaction CRUDL
```

**Security**: Stateless JWT access tokens + opaque refresh tokens stored in DB. Public endpoints: `POST /v1/auth/**` and `GET /actuator/health`. Everything else requires a `Bearer` token.

**Database**: Spring Data JDBC (not JPA). Schema managed by Liquibase at `src/main/resources/db/changelog/`. Add new migrations as numbered SQL files.

**Error handling**: `GlobalExceptionHandler` in `base/` handles all exceptions centrally. Throw specific exceptions (e.g. `EntityNotFoundException`) from services — don't handle them locally in controllers.

## Versioning

Increment `version` in `gradle.properties` for every change to code or configuration before merging to `main`. Use [semantic versioning](https://semver.org): `MAJOR.MINOR.PATCH`.

## Adding a New Feature

1. Update `openapi.yaml`
2. Run `./gradlew openApiGenerate`
3. Add a Liquibase migration if schema changes are needed
4. Implement: Entity → Repository → Mapper → Service → Controller
5. Add integration tests in `src/integrationTest/`

## Testing

- Unit tests: `src/test/java/` — plain Mockito, no base class required
- Integration tests: `src/integrationTest/java/` — extend `BaseMvcIntegrationTest` (HTTP layer) or `BaseIntegrationTest` (repository/service layer)
- Dev seed credentials: username `admin`, password `8a98232f-76f4-4819-b868-91682b52ad3b`

**Required conventions**:
- `var` for all local variable declarations
- Given/When/Then comment sections in every test method
- `ArgumentCaptor` instead of vague `any()` matchers
- `assertThat(result).returns(value, Type::accessor)` for multi-field assertions
- Test methods named `should_<action>_When_<condition>()`
- `@Nested` classes to group tests by method or scenario
