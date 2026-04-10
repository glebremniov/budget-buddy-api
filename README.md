# Budget Buddy API

[![CI](https://github.com/budget-buddy-org/budget-buddy-api/actions/workflows/ci.yaml/badge.svg)](https://github.com/budget-buddy-org/budget-buddy-api/actions/workflows/ci.yaml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=glebremniov_budget-buddy-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=glebremniov_budget-buddy-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=glebremniov_budget-buddy-api&metric=coverage)](https://sonarcloud.io/summary/new_code?id=glebremniov_budget-buddy-api)
[![Release](https://img.shields.io/github/v/release/budget-buddy-org/budget-buddy-api)](https://github.com/budget-buddy-org/budget-buddy-api/releases)

REST API for personal budget management. Built with Spring Boot 4 and PostgreSQL.

## Tech Stack

- **Java 25** / Spring Boot 4.0.5
- **Spring Data JDBC** + PostgreSQL
- **Spring Security** — JWT (access) + opaque refresh tokens
- **Liquibase** — database migrations
- **Budget Buddy Contracts** — external OpenAPI-based contracts
- **Testcontainers** — integration tests
- **Docker** — containerized deployment

## Prerequisites

- Java 25
- Docker & Docker Compose
- GitHub Packages authentication (see [Building](#building))

## Running Locally

The project uses Spring Docker Compose — PostgreSQL starts automatically when you run the app.

```bash
# Clone the repository
git clone https://github.com/your-username/budget-buddy-api.git
cd budget-buddy-api

# Run with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The API will be available at `http://localhost:8080`.

## Running with Docker Compose

```bash
# Copy and fill in environment variables
cp .env.example .env

# Start all services
docker compose up -d

# View logs
docker compose logs -f app
```

### Environment Variables

| Variable | Description |
|---|---|
| `BUDGET_BUDDY_API_IMAGE` | Docker image (e.g. `ghcr.io/username/budget-buddy-api:latest`) |
| `SPRING_PROFILES_ACTIVE` | Spring profile (`prod`) |
| `DB_NAME` | PostgreSQL database name |
| `DB_USER` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `BUDGET_BUDDY_API_ACCESS_TOKEN_SECRET` | JWT signing secret (min 32 characters) |
| `BUDGET_BUDDY_API_ACCESS_TOKEN_VALIDITY_SECONDS` | Access token validity in seconds |
| `BUDGET_BUDDY_API_REFRESH_TOKEN_VALIDITY_SECONDS` | Refresh token validity in seconds |

## API

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/v1/auth/register` | — | Register new user |
| `POST` | `/v1/auth/login` | — | Login, returns token pair |
| `POST` | `/v1/auth/refresh` | — | Refresh access token |
| `POST` | `/v1/auth/logout` | ✓ | Invalidate all refresh tokens |

### Categories

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/v1/categories` | List categories |
| `POST` | `/v1/categories` | Create category |
| `GET` | `/v1/categories/{id}` | Get category |
| `PATCH` | `/v1/categories/{id}` | Update category |
| `DELETE` | `/v1/categories/{id}` | Delete category |

### Transactions

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/v1/transactions` | List transactions (with filters) |
| `POST` | `/v1/transactions` | Create transaction |
| `GET` | `/v1/transactions/{id}` | Get transaction |
| `PATCH` | `/v1/transactions/{id}` | Update transaction |
| `DELETE` | `/v1/transactions/{id}` | Delete transaction |

All endpoints except auth require `Authorization: Bearer <access_token>`.

## Building

To build the project, you need to provide GitHub credentials to access the `budget-buddy-contracts` package. You can set `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables, or provide them via `gradle.properties`:

```bash
# Build and run tests
./gradlew build -Pgpr.user=YOUR_USERNAME -Pgpr.key=YOUR_TOKEN
```

# Build Docker image

```bash
./gradlew bootBuildImage --imageName=ghcr.io/your-username/budget-buddy-api:latest
```

## Tests

```bash
# Run all tests (requires Docker for Testcontainers)
./gradlew test
```

## Health Check

```
GET /actuator/health
```
