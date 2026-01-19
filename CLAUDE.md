# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a sandbox repository for experimenting with Claude Code.

## Project Structure

- `backend/` - Spring Boot API application (Java 21, Maven)
- `frontend/` - Next.js application (TypeScript, pnpm)
- `nginx.conf` - Nginx reverse proxy configuration for production

## Backend API

- **Framework**: Spring Boot 3.4.1
- **Java Version**: 21
- **Package**: `com.sandbox.api`
- **Port**: 8080

### Build Commands

```bash
# Build
cd backend && ./mvnw clean package

# Run
cd backend && ./mvnw spring-boot:run

# Test
cd backend && ./mvnw test
```

### API Endpoints

- `GET /api/hello` - Hello World endpoint

## Development Environment

- **IDE**: IntelliJ IDEA
- **Java**: JDK 21

## Docker Deployment

### Architecture

This project uses nginx as a reverse proxy in production to solve the API URL accessibility issue:

- **Development mode** (`docker-compose up`):
  - Frontend: http://localhost:3000
  - Backend: http://localhost:8080
  - Frontend connects to backend via `http://localhost:8080` (direct access)

- **Production mode** (`docker-compose -f docker-compose.yml up`):
  - Access application via: http://localhost (port 80)
  - Frontend: Internal port 3000 (not exposed externally)
  - Backend: Internal port 8080 (not exposed externally)
  - Nginx reverse proxy routes:
    - `/` → frontend:3000
    - `/api` → backend:8080
  - Frontend connects to backend via `/api` (relative URL through nginx)

### Why Nginx Reverse Proxy?

**Problem**: The frontend's `NEXT_PUBLIC_API_URL` is bundled into browser JavaScript. Using Docker internal hostnames like `http://backend:8080` doesn't work because external browsers cannot resolve them.

**Solution**: Nginx reverse proxy provides:
1. Single access point for users (http://localhost)
2. Frontend and backend on same domain (no CORS issues)
3. Relative URLs work (`/api` resolves to same domain)
4. Production-ready architecture
5. Easy SSL termination in the future

### Running the Application

```bash
# Development mode (with hot reload)
docker-compose up

# Production mode (optimized build)
docker-compose -f docker-compose.yml up

# Rebuild specific service
docker-compose build backend
docker-compose build frontend

# Stop and remove containers
docker-compose down
```

### Development Workflow

#### Managing npm Dependencies

In development mode, `package.json` and `pnpm-lock.yaml` are mounted as volumes, allowing you to add or update dependencies without rebuilding the container:

```bash
# 1. Edit package.json locally (add/update dependencies)
vim frontend/package.json

# 2. Install dependencies inside the running container
docker exec sandbox-frontend pnpm install

# 3. Changes are automatically picked up (no rebuild needed)
```

**Note**: The container must be running for `docker exec` to work. If you need to rebuild anyway, you can also add dependencies and rebuild:

```bash
# Alternative: Add dependency and rebuild
cd frontend && pnpm add <package-name>
docker-compose build frontend
```

## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master