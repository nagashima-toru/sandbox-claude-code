# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Development Rules

- When creating a new Java file, always write JUnit test code for it

## Project Overview

Spring Boot REST API application with PostgreSQL, MyBatis, and Flyway.

## Tech Stack

- **Framework**: Spring Boot 3.4.1
- **Java Version**: 25
- **Database**: PostgreSQL 16
- **ORM**: MyBatis
- **Migration**: Flyway
- **Testing**: JUnit 5, Testcontainers

## Build Commands

```bash
# Build
./mvnw clean package

# Run (requires PostgreSQL via docker-compose)
./mvnw spring-boot:run

# Test (uses Testcontainers - requires Docker/Colima)
./mvnw test

# Integration test with OpenAPI generation (IMPORTANT: use verify, not integration-test)
./mvnw verify

# Compile only
./mvnw compile

# Format code (Google Java Format via Spotless)
./mvnw spotless:apply

# Check code formatting
./mvnw spotless:check
```

**IMPORTANT**: To run integration tests and generate OpenAPI documentation, **always use `./mvnw verify`**.
Do NOT use `./mvnw integration-test` as it will not stop the server properly, causing port conflicts on subsequent runs.

## Code Formatting

This project uses **Spotless** with **Google Java Format** for automatic code formatting.

### Automatic Formatting

Claude Code is configured to automatically format code after editing files via hooks in `.claude/settings.local.json`:

- After `Edit` tool: runs `./scripts/format-code.sh`
- After `Write` tool: runs `./scripts/format-code.sh`

The script automatically detects Java files in the backend and applies Spotless formatting.

### Manual Formatting

If you need to manually format code:

```bash
# Format all Java files
./mvnw spotless:apply

# Check formatting without applying changes
./mvnw spotless:check
```

### CI Integration

- **verify phase**: `spotless:check` ensures code is formatted according to Google Java Format
- **checkstyle**: validates code style compliance (Google Java Style Guide)

Both Spotless and Checkstyle are enforced in CI via `./mvnw verify`.

## Architecture

This project follows **Clean Architecture** with the following layers:

```
com.sandbox.api
├── domain/           # Business logic layer (no external dependencies)
│   ├── model/        # Domain entities
│   └── repository/   # Repository interfaces
│
├── application/      # Application layer
│   └── usecase/      # Use cases (application services)
│
├── infrastructure/   # Infrastructure layer
│   └── persistence/  # Database access (MyBatis mappers, repository implementations)
│
└── presentation/     # Presentation layer
    └── controller/   # REST controllers
```

## Architecture Rules

- Follow Clean Architecture: domain → application → infrastructure (never reverse)
- Application layer must NOT depend on infrastructure layer
- Run `./mvnw verify` after any structural changes to catch ArchitectureTest failures early

**IMPORTANT**: When implementing features, verify import statements don't create forbidden dependencies (e.g., application classes importing from infrastructure package)

### Dependency Rules

- `domain` has no dependencies on other layers
- `application` depends only on `domain`
- `infrastructure` implements `domain` interfaces
- `presentation` uses `application` layer

## Package Structure

| Package                      | Responsibility                              |
|------------------------------|---------------------------------------------|
| `domain.model`               | Domain entities (e.g., Message)             |
| `domain.repository`          | Repository interfaces                       |
| `application.usecase`        | Business use cases                          |
| `infrastructure.persistence` | MyBatis mappers, repository implementations |
| `presentation.controller`    | REST API endpoints                          |

## Database

- **Connection**: `jdbc:postgresql://localhost:5432/sandbox`
- **Credentials**: sandbox/sandbox
- **Migrations**: `src/main/resources/db/migration/`

## API Endpoints

- `GET /api/messages` - Get all messages
- `GET /api/messages/{id}` - Get message by ID
- `POST /api/messages` - Create new message
- `PUT /api/messages/{id}` - Update message
- `DELETE /api/messages/{id}` - Delete message

## バックエンドの開発プロセス（実装〜単体テスト）

1. ユースケース設計
2. OpenAPI仕様作成
3. DB 設計
4. クラス設計
5. 実装 & 単体テスト
6. review

## Testing

**IMPORTANT**: When writing tests, follow the comprehensive test strategy documented in `backend/docs/TEST_STRATEGY.md`.

**IMPORTANT**: After modifying Java code in application/ or domain/ packages, always run `./mvnw test -Dtest=ArchitectureTest` before proceeding

Tests use Testcontainers to automatically start a PostgreSQL container.

### macOS Setup (Colima required)

```bash
brew install colima
colima start
docker context use colima
```

Create `~/.testcontainers.properties`:

```properties
docker.host=unix://${HOME}/.colima/default/docker.sock
```

### Integration Test Best Practices

When writing integration tests, follow these guidelines to avoid common pitfalls:

#### Test Data Setup

- **Use `ON CONFLICT DO UPDATE` for test data**: Instead of `ON CONFLICT DO NOTHING`, use `ON CONFLICT DO UPDATE` to ensure test data is always fresh. This prevents issues where stale data from previous test runs causes authentication or validation failures.

  ```java
  // ✅ Good: Updates existing data
  jdbcTemplate.update(
      "INSERT INTO users (username, password_hash, role, enabled, created_at) "
          + "VALUES (?, ?, 'ADMIN', true, NOW()) "
          + "ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash",
      "testuser",
      passwordEncoder.encode("password123"));

  // ❌ Bad: Leaves stale data
  jdbcTemplate.update(
      "INSERT INTO users (username, password_hash, role, enabled, created_at) "
          + "VALUES (?, ?, 'ADMIN', true, NOW()) ON CONFLICT DO NOTHING",
      "testuser",
      passwordEncoder.encode("password123"));
  ```

- **Generate password hashes individually**: Don't reuse a single password hash variable for multiple users. Generate a fresh hash for each user to avoid bcrypt caching issues.

  ```java
  // ✅ Good: Fresh hash for each user
  jdbcTemplate.update(..., "user1", passwordEncoder.encode("password123"));
  jdbcTemplate.update(..., "user2", passwordEncoder.encode("password123"));

  // ❌ Bad: Reusing the same hash
  String hash = passwordEncoder.encode("password123");
  jdbcTemplate.update(..., "user1", hash);
  jdbcTemplate.update(..., "user2", hash);  // May fail authentication
  ```

- **Leverage `@Transactional`**: Mark test classes with `@Transactional` to automatically rollback changes after each test. This keeps your test database clean without manual cleanup.

#### Common Issues and Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| Login fails in integration test | Stale password hash from previous run | Use `ON CONFLICT DO UPDATE` |
| Authentication randomly fails | Reused password hash variable | Generate fresh hash per user |
| Database pollution between tests | No automatic cleanup | Add `@Transactional` to test class |
| Port conflict on subsequent runs | Using `integration-test` goal | Use `./mvnw verify` instead |
