# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the app (starts PostgreSQL via Docker Compose automatically)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Unit tests
./gradlew test

# Integration tests (requires Docker — uses Testcontainers + PostgreSQL)
./gradlew integrationTest

# Run a single test class
./gradlew test --tests "com.budget.buddy.budget_buddy_api.SomeTest"
./gradlew integrationTest --tests "com.budget.buddy.budget_buddy_api.category.CategoryIntegrationTest"

# All tests + checks
./gradlew check

# Build
./gradlew build

# GitHub Packages credentials are required to resolve the contracts dependency:
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-personal-access-token   # needs read:packages scope
```

## Architecture

### CRUDL framework (`base/crudl/`)

All domain features extend a shared generic hierarchy:

- **Entity:** `BaseEntity` → `AuditableEntity` (auto `createdAt`/`updatedAt`) → `OwnableEntity` (adds `ownerId`)
- **Repository:** `BaseEntityRepository` → `OwnableEntityRepository` (adds owner-scoped finders)
- **Service:** `AbstractBaseEntityService` → `OwnableEntityService` (auto-scopes all queries to `ownerId` from JWT) → domain service
- **Controller:** `BaseEntityController` — provides `createInternal`, `readInternal`, `updateInternal`, `replaceInternal`, `deleteInternal`, `listInternal`. Always override `createdURI()`.
- **Mapper:** `BaseEntityMapper` (MapStruct interface) — provides `toEntity`, `toModel`, `patchEntity` (PATCH, skips nulls/absent JsonNullable), `replaceEntity` (PUT, overwrites all writable fields)
- **Validator:** `BaseEntityValidator<E>` — functional interface; implement and register as a Spring bean to run validation before every save

Controllers implement generated interfaces from `budget-buddy-contracts` (e.g., `TransactionsApi`) and delegate to the `*Internal` methods.

### PATCH / JsonNullable semantics

PATCH operations use `JsonNullable<T>` for fields that can be explicitly nulled:
- **Field omitted** → unchanged (MapStruct `@Condition isPresent` returns false)
- **Field set to `null`** → cleared to null in DB
- **Field set to a value** → updated

The `@Condition isPresent(JsonNullable<?> value)` method in `BaseEntityMapper` gates MapStruct's property-level null strategy.

### Contracts dependency

Controllers implement interfaces generated from the OpenAPI spec in `budget-buddy-contracts`. DTOs (e.g., `Transaction`, `TransactionWrite`, `TransactionUpdate`) come from that package — never define them locally.

### Database

Spring Data JDBC (not JPA). Migrations live in `src/main/resources/db/changelog/migrations/` as numbered SQL files (e.g. `007-...sql`). Register each new file in the master changelog.

### Security

- **Access tokens:** Stateless JWT with `ownerId` claim. `OwnerIdProvider<UUID>` extracts it from `SecurityContext` and is injected into `OwnableEntityService`.
- **Refresh tokens:** Opaque, stored hashed in DB, support revocation via `RefreshTokenService`.
- Public endpoints: `/v1/auth/**`, `/actuator/health`.
- Error responses: RFC 7807 Problem Details (`application/problem+json`) via `GlobalExceptionHandler`. Controllers never catch exceptions.

### Integration tests

- Extend `BaseIntegrationTest` (Testcontainers PostgreSQL, `@Transactional` rollback, `@ActiveProfiles("test")`)
- Extend `BaseMvcIntegrationTest` for HTTP-layer tests (`MockMvcTester`) — provides `registerAndLogin()`, `json()`, `parseBody()` helpers
- Both user isolation and ownership enforcement must be tested (see `CategoryIntegrationTest` for the pattern)

### Adding a domain feature

Create: Entity (extends `OwnableEntity`), Repository (extends `OwnableEntityRepository`), Service (extends `OwnableEntityService`), Controller (extends `BaseEntityController`, implements generated API interface), Mapper (implements `BaseEntityMapper`). Add a Liquibase migration for the new table.

## Commits and Releases

Commit messages must follow [Conventional Commits](https://www.conventionalcommits.org/). A `commit-msg` husky hook enforces this locally via commitlint.

Required format: `type(scope): subject` — scope is optional.

| Type | When to use | Release bump |
|---|---|---|
| `feat` | new user-facing feature | minor |
| `fix` | bug fix | patch |
| `perf` | performance improvement | patch |
| `revert` | reverts a previous commit | patch |
| `feat!` / `BREAKING CHANGE:` footer | breaking API change | major |
| `chore`, `docs`, `test`, `refactor`, `style`, `build`, `ci`, `ops` | everything else | none |

**Releases are fully automated.** Merging to `main` triggers semantic-release in CI, which analyzes commits, bumps the version in `gradle.properties`, writes `CHANGELOG.md`, and publishes a GitHub Release. That release event then triggers the Docker image build and push to GHCR with semver tags and Cosign signing.
