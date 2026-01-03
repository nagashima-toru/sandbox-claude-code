# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Development Rules

- When creating a new Java file, always write JUnit test code for it

## Project Overview

Spring Boot REST API application with PostgreSQL, MyBatis, and Flyway.

## Tech Stack

- **Framework**: Spring Boot 3.4.1
- **Java Version**: 23
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

# Compile only
./mvnw compile
```

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

### Dependency Rules

- `domain` has no dependencies on other layers
- `application` depends only on `domain`
- `infrastructure` implements `domain` interfaces
- `presentation` uses `application` layer

## Package Structure

| Package | Responsibility |
|---------|----------------|
| `domain.model` | Domain entities (e.g., Message) |
| `domain.repository` | Repository interfaces |
| `application.usecase` | Business use cases |
| `infrastructure.persistence` | MyBatis mappers, repository implementations |
| `presentation.controller` | REST API endpoints |

## Database

- **Connection**: `jdbc:postgresql://localhost:5432/sandbox`
- **Credentials**: sandbox/sandbox
- **Migrations**: `src/main/resources/db/migration/`

## API Endpoints

- `GET /api/message` - Returns message from database

## Testing

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
