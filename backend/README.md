# Backend API

Spring Boot API application with PostgreSQL, MyBatis, and Flyway.

## Technology Stack

- **Framework**: Spring Boot 4.1.2
- **Java Version**: 25
- **Build Tool**: Maven
- **Database**: PostgreSQL 16
- **ORM**: MyBatis
- **Migration**: Flyway
- **Testing**: JUnit 5, Testcontainers
- **Package**: `com.sandbox.api`
- **Port**: 8080

## Architecture

This project follows **Clean Architecture** principles with the following layers:

- **Domain layer** - Core business logic and entities (no external dependencies)
- **Application layer** - Use cases and application services
- **Infrastructure layer** - Database access (MyBatis mappers, repository implementations)
- **Presentation layer** - REST controllers

## Requirements

- Java 25
- Docker Engine (any distribution: Docker Engine, Colima, Docker Desktop, etc.)

## Docker Setup

Tests use Testcontainers, which needs a running Docker Engine. Docker Desktop is not required.

Testcontainers finds the engine in this order: `DOCKER_HOST`, `docker.host` in `~/.testcontainers.properties`, then `/var/run/docker.sock`.

`TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock` in `pom.xml` tells Ryuk (container cleanup) which socket path to mount inside the engine. It is required for Colima and harmless for the others.

| OS | Engine | Verified |
| --- | --- | --- |
| macOS | Colima | 2026-11, Engine 28.4.0 |
| macOS | Docker Desktop | 2026-11, Engine 29.6.1 |
| Linux | Docker Engine | CI (GitHub Actions `ubuntu-latest`) |
| Windows (WSL2) | Docker Engine in WSL2 | Not verified |
| Windows | Docker Desktop | Not verified |

### macOS

Docker Desktop: install and start it. No extra configuration is needed.

Colima (no Docker Desktop needed):

```bash
brew install colima docker docker-compose  # follow brew caveats to enable `docker compose`
colima start
export DOCKER_HOST=unix://${HOME}/.colima/default/docker.sock  # add to ~/.zshrc
```

If Docker Desktop is also installed, both can run at the same time. `DOCKER_HOST` decides which one is used.
Without it, `/var/run/docker.sock` (usually Docker Desktop) is used.

### Linux

Install Docker Engine: <https://docs.docker.com/engine/install/>

```bash
sudo usermod -aG docker $USER  # log out and back in
```

### Windows (WSL2)

Install Docker Engine inside the WSL2 distribution (same steps as Linux), clone the repository in the WSL2 file system, and run all commands there.

Native Windows (`mvnw.cmd`) needs an engine that runs Linux containers on Windows, such as Docker Desktop.

## Build & Run

```bash
# Build
./mvnw clean package

# Run (requires PostgreSQL)
docker compose -f ../docker-compose.yml up -d
./mvnw spring-boot:run
```

## Test

Tests use Testcontainers to automatically start a PostgreSQL container.

```bash
./mvnw test
```

## API Endpoints

- `GET /api/message` - Returns message from database
