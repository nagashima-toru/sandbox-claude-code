# Backend API

Spring Boot API application with PostgreSQL, MyBatis, and Flyway.

## Technology Stack

- **Framework**: Spring Boot 3.4.1
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
- Docker (or Colima on macOS)

## Docker Setup

### macOS

Docker Desktop has compatibility issues with Testcontainers. Use Colima instead:

```bash
# Install Colima
brew install colima

# Start Colima (stop Docker Desktop first)
colima start

# Set Docker context
docker context use colima
```

Create `~/.testcontainers.properties`:

```properties
docker.host=unix://${HOME}/.colima/default/docker.sock
```

### Windows

Install Docker Desktop:

1. Download from <https://www.docker.com/products/docker-desktop/>
2. Run installer and follow instructions
3. Restart PC if required

### Linux

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install docker.io docker-compose-plugin
sudo usermod -aG docker $USER

# Start Docker
sudo systemctl start docker
sudo systemctl enable docker

# Log out and log back in for group changes to take effect
```

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
