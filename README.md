# Sandbox Claude Code

A sandbox repository for experimenting with Claude Code.

## Project Structure

```
sandbox-claude-code/
├── backend/          # Spring Boot API application
└── CLAUDE.md        # Project guidance for Claude Code
```

## Backend API

The backend is a Spring Boot application built with Java 23 and Maven.

### Technology Stack

- **Framework**: Spring Boot 3.4.1
- **Java Version**: 23
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **ORM**: MyBatis
- **Migration**: Flyway
- **Package**: `com.sandbox.api`
- **Port**: 8080

### Prerequisites

- JDK 23
- Maven (or use the included Maven wrapper)

### Getting Started

#### Build the application

```bash
cd backend
./mvnw clean package
```

#### Run the application

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

#### Run tests

```bash
cd backend
./mvnw test
```

### API Endpoints

- `GET /api/hello` - Hello World endpoint

## Development

### IDE Setup

This project is configured for IntelliJ IDEA with Java 23.

### Architecture

The backend follows Clean Architecture principles with:
- Domain layer
- Application layer
- Infrastructure layer

## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master

## License

This is a sandbox project for experimentation purposes.