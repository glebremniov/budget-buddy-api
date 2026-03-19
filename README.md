# Budget Buddy API

REST API for personal budget management. Built with Spring Boot 4 and PostgreSQL.

## Tech Stack

- **Java 25** / Spring Boot 4.0.4
- **Spring Data JDBC** + PostgreSQL
- **Spring Security** — JWT (access) + opaque refresh tokens
- **Liquibase** — database migrations
- **OpenAPI Generator** — API-first development
- **Testcontainers** — integration tests
- **Docker** — containerized deployment

## Prerequisites

- Java 25
- Docker & Docker Compose

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

Dev seed data is applied automatically via Liquibase:
- Username: `admin`
- Password: `8a98232f-76f4-4819-b868-91682b52ad3b`

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
| `BUDGET_BUDDY_API_JWT_SECRET` | JWT signing secret |
| `BUDGET_BUDDY_API_JWT_ACCESS_TOKEN_VALIDITY_SECONDS` | Access token validity in seconds |
| `BUDGET_BUDDY_API_JWT_REFRESH_TOKEN_VALIDITY_SECONDS` | Refresh token validity in seconds |

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

```bash
# Build and run tests
./gradlew build

# Build Docker image
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
