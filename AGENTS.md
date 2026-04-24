# Agent Conventions & Guidance

This file provides guidance for AI agents (Claude Code, Junie, etc.) when working with this repository.

---

## Quick Start

**Stack:** Java 25, Spring Boot 4.0.5, Spring Data JDBC, PostgreSQL, Liquibase, MapStruct, Lombok

**Prerequisites:** The `budget-buddy-contracts` dependency is fetched from GitHub Packages. Set these before building:
```bash
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-personal-access-token   # needs read:packages scope
```
Or add `gpr.user` / `gpr.key` to `~/.gradle/gradle.properties`.

**Run locally:**
```bash
# Automatically starts PostgreSQL via Docker Compose (dev profile)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Test:**
```bash
./gradlew test                  # all tests (unit + integration)
./gradlew integrationTest       # integration tests (requires Docker)
./gradlew check                 # all tests + quality checks
```

**Build:**
```bash
./gradlew build
```

---

## Agent Conventions

### Error Handling

- **RFC 9457 (Problem Details)**: All error responses must follow the Problem Details for HTTP APIs specification.
- **Standardized Titles**: When `type` is `about:blank`, the `title` SHOULD be the same as the HTTP status phrase (e.g., "Bad Request" for 400).
- **Field-Level Errors**: Validation exceptions (`MethodArgumentNotValidException`, `ConstraintViolationException`) must return a `Problem` containing an `errors` array with `field` and `message` properties.
- **Request URI**: The `instance` field should contain the current request URI, retrieved using `ServletWebRequest` in `GlobalExceptionHandler`.

### Security & OIDC

- **Stateless resource server**: The API validates JWTs from an external OIDC provider. No sessions, no server-side token storage.
- **JIT user provisioning**: `OidcUserProvisioningFilter` runs after `BearerTokenAuthenticationFilter` and maps JWT `sub` + `iss` claims to a local user via an atomic upsert. The resolved user UUID is stored as a request attribute.
- **Multi-issuer support**: Users are identified by the composite `(oidc_subject, oidc_issuer)` unique constraint. The same `sub` from different issuers creates separate users.
- **Audience validation**: JWTs must contain an expected audience configured via `OIDC_AUDIENCES`.
- **Ownership isolation**: All ownable entities are scoped to the authenticated user via `OwnerIdProvider<UUID>`, which reads the user UUID set by the provisioning filter.
- **No PII in logs**: Never log OIDC subjects or other user-identifying claims at INFO level or above. Use DEBUG.

### Code Patterns

- **Problem Details Extension**: The base `Problem` class from `budget-buddy-contracts` now includes an `errors` field for field-level validation errors. Use it directly instead of extending it for common validation cases.
- **Validation Handling**:
  - Prefer using `ex.getBindingResult().getFieldErrors()` for `MethodArgumentNotValidException`.
  - Prefer using `ex.getConstraintViolations()` for `ConstraintViolationException`.
- **Transactional Methods**:
  - **Read-Only Operations**: All service-level read operations (e.g., `read`, `list`, `count`) MUST be marked with `@Transactional(readOnly = true)`.
  - **Class-Level Default**: Prefer setting `@Transactional(readOnly = true)` at the class level and overriding it with `@Transactional` on specific write methods (create, update, delete).

---

## Development Notes

### Testing Locally

1. Run `./gradlew test` for quick all-tests feedback
2. Use `./gradlew integrationTest --tests "..."` to debug specific integration tests
3. Leverage `./gradlew bootRun --args='--spring.profiles.active=dev'` to test full app locally

### Common Tasks

- **Run a single test:** `./gradlew test --tests "com.budget.buddy.budget_buddy_api.CategoryServiceTest"`
- **Run integration test:** `./gradlew integrationTest --tests "com.budget.buddy.budget_buddy_api.CategoryControllerIT"`
- **Full verification:** `./gradlew check` (runs all tests, linters, SonarQube analysis)

### Available Skills (Claude-specific)

Use these slash commands for common workflows:

| Command | What it does |
|---|---|
| `/new-feature <domain>` | Scaffold a full CRUDL domain feature end-to-end |
| `/add-migration <description>` | Create a numbered Liquibase migration and register it |
| `/run-tests [scope]` | Run tests and surface failures |
| `/ship` | Commit all changes and open a PR against `main` |
| `/javadoc` | Add Javadoc to public/protected API in recently changed files |


