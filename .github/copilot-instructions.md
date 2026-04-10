# Copilot Instructions for Budget Buddy API

This guide helps Copilot sessions work effectively in this Java/Spring Boot REST API repository.

## Quick Reference: Commands

```bash
# Running locally (auto-starts PostgreSQL)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Testing
./gradlew test                     # Unit tests only
./gradlew integrationTest          # Integration tests (Testcontainers + PostgreSQL)
./gradlew check                    # All tests + quality checks + SonarQube analysis

# Running specific tests
./gradlew test --tests "com.budget.buddy.budget_buddy_api.CategoryServiceTest"
./gradlew integrationTest --tests "com.budget.buddy.budget_buddy_api.CategoryControllerIT"

# Building & versioning
./gradlew build                    # Build JAR
./gradlew bootBuildImage --imageName=ghcr.io/your-username/budget-buddy-api:latest  # Docker image

# Always increment version in gradle.properties before merging to main (use semantic versioning)
```

## Environment Setup

### Prerequisites

- **Java 25** — required by `toolchain` in `build.gradle.kts`
- **Docker & Docker Compose** — for PostgreSQL and integration tests
- **GitHub credentials** — required to fetch `budget-buddy-contracts` from GitHub Packages

### GitHub Packages Authentication

The project depends on `com.budgetbuddy:budget-buddy-contracts` from GitHub Packages. Authenticate using one of:

**Option 1: Environment variables (preferred for CI/CD)**
```bash
export GITHUB_ACTOR=your-username
export GITHUB_TOKEN=your-personal-access-token
./gradlew build
```

**Option 2: gradle.properties (for local development)**
```
# ~/.gradle/gradle.properties or ./gradle.properties
gpr.user=your-username
gpr.key=your-personal-access-token
```

> **Token requirements:** Personal access token must have `read:packages` scope. See [GitHub docs](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-with-a-personal-access-token).

### Local Development with Docker Compose

For development without the auto-starting Spring Docker Compose (e.g., to test with custom PostgreSQL settings), use the local compose file:

```bash
# Copy environment template
cp .env.example .env

# Start PostgreSQL + pgAdmin (optional)
docker compose -f docker-compose.local.yaml up -d

# Run the app with dev profile (connects to Docker PostgreSQL)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Stop services
docker compose -f docker-compose.local.yaml down
```

**Environment variables in `.env`:**
- `DB_NAME` — PostgreSQL database name
- `DB_USER` — PostgreSQL username
- `DB_PASSWORD` — PostgreSQL password
- `DB_IMAGE_TAG` — PostgreSQL image version (e.g., `18.3-alpine`)
- `DOCKER_PLATFORM` — target platform (e.g., `linux/arm64` for Mac M1/M2)


## Architecture Overview

### API-First Design

**External OpenAPI Contracts**: The API specification is managed by the external [`budget-buddy-contracts`](https://github.com/budget-buddy-org/budget-buddy-contracts) repository. This project imports the generated models and interfaces via the `com.budgetbuddy:budget-buddy-contracts` dependency (version specified in `build.gradle.kts`, currently `1.0.1`).

When the API contract changes:
1. Update the `budget-buddy-contracts` repository with the new OpenAPI spec
2. Bump `budgetBuddyContractsVersion` in `build.gradle.kts`
3. Run `./gradlew build` to fetch the new contracts
4. Implement the business logic to satisfy the new contract

This approach decouples API contracts from implementation, allowing multiple services to consume the same contracts.


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

### Security Configuration

**JWT Implementation:**
- Access tokens: Short-lived JWTs containing `ownerId` claim for multi-tenant filtering
- Refresh tokens: Opaque tokens stored in PostgreSQL (enables revocation)
- Token generation/parsing in `security/jwt/` package
- Refresh token management in `security/refresh/` package

**Endpoint Security:**
- **Public endpoints:** `POST /v1/auth/register`, `POST /v1/auth/login`, `POST /v1/auth/refresh`, `GET /actuator/health`
- **Protected endpoints:** All others require `Authorization: Bearer <access_token>` header
- Multi-tenant isolation: `OwnableEntityService` automatically filters all queries by `ownerId` extracted from JWT

**Token Configuration (environment variables):**
- `BUDGET_BUDDY_API_ACCESS_TOKEN_SECRET` — JWT signing key (min 32 chars for HS256)
- `BUDGET_BUDDY_API_ACCESS_TOKEN_VALIDITY_SECONDS` — Access token expiry (suggested: 900 seconds / 15 minutes)
- `BUDGET_BUDDY_API_REFRESH_TOKEN_VALIDITY_SECONDS` — Refresh token expiry (suggested: 1209600 seconds / 14 days)

### Database Migrations

**Schema management:** Liquibase at `src/main/resources/db/changelog/`

**File structure:**
- `db.changelog-master.yaml` — Master changelog referencing all migrations
- `migrations/` — Numbered SQL migration files (e.g., `001-initial-schema.sql`, `002-refresh-tokens.sql`)

**Adding a migration:**
1. Create new SQL file in `migrations/` with next number (e.g., `007-add-column.sql`)
2. Add changeset entry in `db.changelog-master.yaml`:
   ```yaml
   - include:
       file: migrations/007-add-column.sql
   ```
3. Migration auto-runs on next application startup
4. Migrations are tracked in `databasechangelog` table (do not modify manually)

**Migration best practices:**
- Keep migrations idempotent (safe to run multiple times)
- Use `IF NOT EXISTS` and `IF EXISTS` clauses where applicable
- Test migrations locally before committing
- Never edit existing migration files; create new ones instead



### Adding a New Feature

1. **If API contract changes:** Update the `budget-buddy-contracts` repository, bump the version in `build.gradle.kts`, then run `./gradlew build` to fetch new contracts
2. **Add Liquibase migration** if schema changes needed (numbered SQL files in `src/main/resources/db/changelog/migrations/`)
3. **Implement in order:** Entity → Repository → Mapper → Service → Controller
4. **Add integration tests** in `src/integrationTest/java/`
5. **Increment version** in `gradle.properties` before merging
6. **Run full test suite:** `./gradlew check`

### Accessing External Contracts

- The project depends on `com.budgetbuddy:budget-buddy-contracts` from GitHub Packages
- Requires GitHub credentials: set `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables (or `gpr.user`/`gpr.key` in `gradle.properties`)
- Models and interfaces become available after dependencies are resolved in the build


## Versioning

- Semantic versioning (MAJOR.MINOR.PATCH) in `gradle.properties`
- Increment **before** every merge to `main` for any code or configuration changes
- Patch version only; minor/major versions set manually as needed

## Configuration & Profiles

- **dev profile:** Includes Spring Boot Docker Compose auto-starter; PostgreSQL container launches on startup
- **prod profile:** Used in Docker deployments; all environment-specific configs loaded from env vars
- Spring profiles active: set via `--spring.profiles.active=dev` or `SPRING_PROFILES_ACTIVE` env var

## Continuous Integration & Deployment

### GitHub Actions Workflows

All workflows are in `.github/workflows/`:

**`ci.yaml`** — Runs on every push to `main` and pull request:
- Sets up JDK 25 and Gradle build cache (via `gradle/actions/setup-gradle`)
- Runs `./gradlew build sonar` (unit tests + SonarQube analysis)
- Caches SonarQube packages
- Uploads JAR artifact to GitHub (1-day retention)
- **Triggers on:** Code changes (`src/`, `build.gradle*`, `gradle/`, `docker-compose*.yaml`)
- **Requires:** `SONAR_TOKEN` secret, `GITHUB_TOKEN` (auto-provided)

**`release.yaml`** — Triggered automatically when CI passes on `main` (`workflow_run`):
- `setup` job: reads version from `gradle.properties`, checks if the tag already exists
- `docker-build` job: builds arm64 and amd64 Docker images in parallel (matrix, `fail-fast: false`), pushes each to GHCR
- `docker-manifest` job: assembles and pushes a multi-arch manifest + `latest` tag via `docker buildx imagetools`
- `release` job: creates a git tag and GitHub release with auto-generated notes
- All jobs after `setup` are skipped if the tag already exists (idempotent)
- **Requires:** `GITHUB_TOKEN` (auto-provided)

**`dependency-submission.yaml`** — Automatically generates dependency graph for GitHub's supply chain security features

### SonarQube Integration

- Configuration in `build.gradle.kts` (sonar task and properties block)
- Automatic Jacoco code coverage XML report generation
- Excludes `**/generated/**` from coverage calculations
- Requires `SONAR_TOKEN` secret in repository settings
- Project key: `glebremniov_budget-buddy-api` (organization: `budget-buddy-org`)
- [View dashboard](https://sonarcloud.io/summary/new_code?id=glebremniov_budget-buddy-api)

### Docker Deployment

**Building Docker image locally:**
```bash
./gradlew bootBuildImage --imageName=ghcr.io/your-username/budget-buddy-api:latest
```

**Deploying with Docker Compose:**
```bash
# Copy and configure environment
cp .env.example .env
# Edit .env: set BUDGET_BUDDY_API_IMAGE, JWT secrets, database credentials, etc.

# Start all services (API + PostgreSQL)
docker compose up -d

# View logs
docker compose logs -f app

# Health check
curl http://localhost:8080/actuator/health

# Stop services
docker compose down
```

**Environment variables for production deployment** (set in `.env`):
| Variable | Example | Notes |
|---|---|---|
| `BUDGET_BUDDY_API_IMAGE` | `ghcr.io/user/budget-buddy-api:0.1.13` | Docker image URI |
| `SPRING_PROFILES_ACTIVE` | `prod` | Use `prod` for production |
| `BUDGET_BUDDY_API_ACCESS_TOKEN_SECRET` | 32+ random characters | JWT signing secret; min 32 chars |
| `BUDGET_BUDDY_API_ACCESS_TOKEN_VALIDITY_SECONDS` | `900` | Token expiry in seconds (15 min) |
| `BUDGET_BUDDY_API_REFRESH_TOKEN_VALIDITY_SECONDS` | `1209600` | Refresh token expiry (14 days) |
| `DB_NAME` | `budget_buddy_db` | PostgreSQL database name |
| `DB_USER` | `budget_buddy_user` | PostgreSQL username |
| `DB_PASSWORD` | strong-random-password | PostgreSQL password |
| `DB_IMAGE_TAG` | `18.3-alpine` | PostgreSQL image version |
| `DOCKER_PLATFORM` | `linux/arm64` | Target platform (`linux/arm64` for Mac M1/M2, `linux/amd64` for Intel) |
