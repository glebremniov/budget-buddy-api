# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**For comprehensive documentation on architecture, testing, versioning, and code conventions, see [.github/SHARED.md](.github/SHARED.md).**

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
./gradlew test                  # unit tests only
./gradlew integrationTest       # integration tests (requires Docker)
./gradlew check                 # all tests + quality checks
```

**Build:**
```bash
./gradlew build
```

---

## Claude-Specific Notes

### Dev Seed Credentials

For testing with the dev profile:
- **Username:** `admin`
- **Password:** `8a98232f-76f4-4819-b868-91682b52ad3b`

Use these to test auth flows in local development.

### Testing Locally

When working with Claude Code:
1. Run `./gradlew test` for quick unit test feedback
2. Use `./gradlew integrationTest --tests "..."` to debug specific integration tests
3. Leverage `./gradlew bootRun --args='--spring.profiles.active=dev'` to test full app locally

### Common Tasks

- **Run a single test:** `./gradlew test --tests "com.budget.buddy.budget_buddy_api.CategoryServiceTest"`
- **Run integration test:** `./gradlew integrationTest --tests "com.budget.buddy.budget_buddy_api.CategoryControllerIT"`
- **Full verification:** `./gradlew check` (runs all tests and linters)

### Available Skills

Use these slash commands for common workflows:

| Command | What it does |
|---|---|
| `/new-feature <domain>` | Scaffold a full CRUDL domain feature end-to-end |
| `/add-migration <description>` | Create a numbered Liquibase migration and register it |
| `/run-tests [scope]` | Run tests and surface failures |
| `/ship` | Commit all changes and open a PR against `main` |
| `/javadoc` | Add Javadoc to public/protected API in recently changed files |
| `/sync-postman` | Sync `openapi.yaml` to the Postman "Budget Buddy API" spec |

---

## Reference

For details on:
- **Architecture & CRUDL framework** → see [.github/SHARED.md#architecture](.github/SHARED.md#architecture)
- **Testing conventions** → see [.github/SHARED.md#testing-conventions](.github/SHARED.md#testing-conventions)
- **Adding new features** → see [.github/SHARED.md#adding-a-new-feature](.github/SHARED.md#adding-a-new-feature)
- **Code conventions** → see [.github/SHARED.md#code-conventions](.github/SHARED.md#code-conventions)

For Copilot CLI (GitHub Copilot in terminal), see [.github/copilot-instructions.md](.github/copilot-instructions.md) for expanded documentation on environment setup, CI/CD, and Docker deployment.
