# Copilot Instructions for Budget Buddy API

This guide helps Copilot sessions work effectively in this Java/Spring Boot REST API repository.

## Quick Reference: Commands

```bash
# Code generation
./gradlew openApiGenerate          # Generate DTOs and interfaces from OpenAPI spec (run after spec changes)

# Running
./gradlew bootRun --args='--spring.profiles.active=dev'

# Testing
./gradlew test                     # Unit tests only
./gradlew integrationTest          # Integration tests (Testcontainers + PostgreSQL)
./gradlew check                    # All tests + quality checks

# Running specific tests
./gradlew test --tests "com.budget.buddy.budget_buddy_api.CategoryServiceTest"
./gradlew integrationTest --tests "com.budget.buddy.budget_buddy_api.CategoryControllerIT"

# Building & versioning
./gradlew build                    # Build JAR
./gradlew bootBuildImage --imageName=ghcr.io/your-username/budget-buddy-api:latest  # Docker image

# Always increment version in gradle.properties before merging to main (use semantic versioning)
```

## Architecture Overview

### API-First Design

**`src/main/resources/openapi.yaml`** is the authoritative API specification (managed by [budget-buddy-contracts](https://github.com/glebremniov/budget-buddy-contracts) package). Any endpoint or model change must:
1. Update the OpenAPI spec
2. Run `./gradlew openApiGenerate` to regenerate DTOs, request/response models, and controller interfaces
3. Then implement the business logic

Generated code lives in `build/generated/sources/openapi/` (do not edit directly).

### Generic CRUDL Framework

All domain entities use a reusable **framework** defined in `base/crudl/`:

**Core classes hierarchy:**
- `BaseEntity` — interface for all entities; extended by:
  - `AuditableEntity` — auto-populated `createdAt`/`updatedAt` timestamps (managed by `BaseEntityListener`)
  - `OwnableEntity` — adds `ownerId` field for **multi-tenant isolation** (all queries auto-filtered by owner)
- `BaseEntityRepository` — Spring Data JDBC interfaces (handles persistence)
- `AbstractBaseEntityService` → `OwnableEntityService` (auto-filters all operations by `ownerId`)
- `BaseEntityController` — delegates to five internal methods: `createInternal()`, `readInternal()`, `updateInternal()`, `replaceInternal()`, `deleteInternal()`, `listInternal()`
- `BaseEntityMapper` — MapStruct mappers for Entity ↔ DTO conversion
- `BaseEntityValidator` — custom validators autowired as a `Set` into services

**Adding a new CRUDL entity:**
1. Entity → Repository → Mapper → Service → Controller (in that order)
2. Controllers extend `BaseEntityController` and delegate to `*Internal()` methods
3. Services extend `OwnableEntityService`
4. Always override `createdURI(entity)` in controllers
5. Controllers implement the **generated OpenAPI interface**

### Package Layout

```
base/                    # Infrastructure: config, exception handling, CRUDL framework
├── crudl/              # Base classes for domain entities
├── config/             # Spring security, bean configs
├── exception/          # Exception classes (EntityNotFoundException, etc.)
└── validation/         # Custom validators
user/                   # User management (registration, profile)
security/              # Authentication & authorization
├── auth/              # Login/logout/register endpoints
├── jwt/               # JWT token generation/parsing
└── refresh/           # Refresh token management & storage
category/              # Category CRUDL (extends OwnableEntityService)
transaction/           # Transaction CRUDL (extends OwnableEntityService)
```

### Security Model

**Stateless JWT architecture:**
- **Access token** — short-lived JWT, includes `ownerId` claim for multi-tenant filtering
- **Refresh token** — opaque token stored in database (to enable revocation)
- **Public endpoints** — `POST /v1/auth/**` (register/login/refresh), `GET /actuator/health`
- **All other endpoints** — require `Authorization: Bearer <access_token>` header
- **Multi-tenancy** — `OwnableEntityService` automatically filters all queries by `ownerId` from the JWT

### Database & Migrations

- **ORM:** Spring Data JDBC (not JPA; simpler, no N+1 queries)
- **Schema management:** Liquibase at `src/main/resources/db/changelog/`
  - Master changelog: `db.changelog-master.yaml`
  - Migrations: `migrations/` folder (numbered SQL files)
  - Add new schema changes as numbered migrations; Liquibase auto-detects at startup
- **Dev seed data** — see test credentials below

### Error Handling

- `EntityNotFoundException` — thrown by services when entity not found (auto-translated to HTTP 404)
- Central error handling in `base/` exception handlers
- **Never** catch exceptions in controllers — let them bubble up for centralized handling
- Services throw specific exceptions; controllers don't handle them

## Testing Conventions

### Unit Tests (`src/test/java/`)
- Plain Mockito; no base class required
- Test method naming: `should_<action>_When_<condition>()`
- Use `var` for all local variable declarations
- Structure with comment sections: `// Given`, `// When`, `// Then`
- Use `ArgumentCaptor` instead of vague `any()` matchers
- Use `assertThat(result).returns(value, Type::accessor)` for multi-field assertions
- Organize with `@Nested` classes by method or scenario

### Integration Tests (`src/integrationTest/java/`)
- Extend `BaseMvcIntegrationTest` (HTTP layer) or `BaseIntegrationTest` (repository/service layer)
- Testcontainers automatically spin up PostgreSQL
- **Dev seed credentials:** username `admin`, password `8a98232f-76f4-4819-b868-91682b52ad3b`
- Same naming and structure conventions as unit tests

### Running Tests in CI

- GitHub Actions workflows in `.github/workflows/`
- GitHub Packages authentication required (`GITHUB_ACTOR`, `GITHUB_TOKEN`)
- **SonarQube integration** — coverage reports auto-uploaded; excludes `generated/` sources

## Key Patterns & Conventions

### Variable Naming
- Use `var` for all local variable declarations (language feature, not a style choice)

### Dependency Injection
- Constructor injection only; fields are `final`
- Autowiring of validators and services via constructor

### Mapper Usage
- MapStruct mappers handle Entity ↔ DTO conversions
- Entity models (JPA/JDBC) kept separate from API models (OpenAPI-generated)

### Validators
- Custom validators implement `Validator` and are autowired as `Set<Validator>` into services
- Each validator is responsible for one domain rule

### Lombok Usage
- `@Getter`, `@Setter`, `@Data`, `@Builder` for reducing boilerplate
- MapStruct processors configured to work with Lombok

## Development Workflow

### Adding a New Feature

1. **Update OpenAPI spec** in `budget-buddy-contracts` repository
2. **Run code generation:** `./gradlew openApiGenerate`
3. **Add Liquibase migration** if schema changes needed
4. **Implement in order:** Entity → Repository → Mapper → Service → Controller
5. **Add integration tests** in `src/integrationTest/java/`
6. **Increment version** in `gradle.properties` before merging
7. **Run full test suite:** `./gradlew check`

### Accessing External Contracts

- The project depends on `com.budgetbuddy:budget-buddy-contracts` from GitHub Packages
- Requires GitHub credentials: set `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables (or `gpr.user`/`gpr.key` in `gradle.properties`)
- Generated models and interfaces are available after `openApiGenerate` runs

## Versioning

- Semantic versioning (MAJOR.MINOR.PATCH) in `gradle.properties`
- Increment **before** every merge to `main` for any code or configuration changes
- Patch version only; minor/major versions set manually as needed

## Configuration & Profiles

- **dev profile:** Includes Spring Boot Docker Compose auto-starter; PostgreSQL container launches on startup
- **prod profile:** Used in Docker deployments; all environment-specific configs loaded from env vars
- Spring profiles active: set via `--spring.profiles.active=dev` or `SPRING_PROFILES_ACTIVE` env var
