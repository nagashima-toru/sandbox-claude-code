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

### Overview

This project uses a two-tier Docker Compose configuration:
- `docker-compose.yml` - Base configuration (production setup)
- `docker-compose.override.yml` - Development overrides (automatically applied)

Docker Compose automatically merges these files when you run `docker-compose up`, giving you a development environment with hot reload. To test the production setup, explicitly specify only the base file.

### Development Mode (Default) - `docker-compose up`

**When to use**: Daily development work

```bash
docker-compose up
```

**What it does**:
- Automatically uses `docker-compose.yml` + `docker-compose.override.yml`
- Builds from Dockerfile `development` target
- Enables hot reload for both frontend and backend
- Mounts source code into containers for live updates
- Exposes debug port (5005) for backend debugging
- Frontend runs with `pnpm dev`
- Backend runs with Spring Boot DevTools

**Configuration**:
- Frontend: http://localhost:3000 (accessible)
- Backend: http://localhost:8080 (accessible)
- Nginx: http://localhost:80 (accessible, but usually use :3000 for dev)
- Backend Debug Port: 5005
- Frontend connects to backend via `http://localhost:8080` (direct browser access)
- Runs as root user (for easier development)
- Node environment: development

**Pros**:
- Fast feedback loop with hot reload
- Easy debugging
- No rebuild needed for code changes

**Cons**:
- Not representative of production environment
- Larger image size
- Development dependencies included

### Production-like Testing - `docker-compose -f docker-compose.yml up`

**When to use**: Testing production Docker build locally before deployment

```bash
docker-compose -f docker-compose.yml up
```

**What it does**:
- Uses only `docker-compose.yml` (no override file)
- Builds from Dockerfile `production` target
- Optimized production build
- No source code mounts
- No hot reload
- No debug ports
- Frontend runs with `pnpm start` (production server)

**Configuration**:
- Access via: http://localhost (port 80 only)
- Frontend: Internal port 3000 (not exposed externally)
- Backend: Internal port 8080 (not exposed externally)
- Nginx reverse proxy routes:
  - `/` → frontend:3000
  - `/api` → backend:8080
- Frontend connects to backend via `/api` (relative URL through nginx)
- Runs as non-root user (nextjs) for security
- Node environment: production

**Pros**:
- Tests actual production configuration
- Smaller image size
- Security best practices (non-root user)
- Representative of production behavior

**Cons**:
- Requires rebuild for any code changes
- No hot reload
- Slower iteration cycle

### Architecture

#### Why Nginx Reverse Proxy?

**Problem**: The frontend's `NEXT_PUBLIC_API_URL` is bundled into browser JavaScript during build time. Using Docker internal hostnames like `http://backend:8080` doesn't work because external browsers cannot resolve them.

**Solution**: Nginx reverse proxy provides:
1. Single access point for users (http://localhost)
2. Frontend and backend on same domain (no CORS issues)
3. Relative URLs work (`/api` resolves to same domain)
4. Production-ready architecture
5. Easy SSL termination in the future

### Common Commands

```bash
# Development mode (with hot reload)
docker-compose up

# Development mode in background
docker-compose up -d

# Production-like mode (optimized build)
docker-compose -f docker-compose.yml up

# Rebuild specific service (development mode)
docker-compose build backend
docker-compose build frontend

# Rebuild specific service (production mode)
docker-compose -f docker-compose.yml build backend
docker-compose -f docker-compose.yml build frontend

# View logs
docker-compose logs -f
docker-compose logs -f frontend
docker-compose logs -f backend

# Stop and remove containers
docker-compose down

# Stop and remove containers with volumes
docker-compose down -v
```

### Common Workflows

#### Adding a New npm Package

**Development mode** (recommended - faster):

In development mode, `package.json` and `pnpm-lock.yaml` are mounted as volumes, allowing you to add or update dependencies without rebuilding the container:

```bash
# 1. Edit package.json locally (add/update dependencies)
vim frontend/package.json

# 2. Install dependencies inside the running container
docker exec sandbox-frontend pnpm install

# 3. Changes are automatically picked up (no rebuild needed)
```

**Alternative method** (rebuild):

If the container is not running or you prefer to rebuild:

```bash
# 1. Add package to frontend/package.json manually or:
cd frontend && pnpm add <package-name>

# 2. Rebuild frontend container to include new dependency
docker-compose build frontend

# 3. Restart
docker-compose up
```

**Note**: The container must be running for `docker exec` to work.

#### Changing Dockerfile Configuration

```bash
# 1. Edit Dockerfile
# 2. Rebuild the affected service
docker-compose build <service-name>

# 3. Restart
docker-compose up
```

#### Debugging Backend in IDE

```bash
# 1. Start in development mode
docker-compose up

# 2. Configure IntelliJ IDEA remote debugging:
#    - Host: localhost
#    - Port: 5005
#    - Debugger mode: Attach to remote JVM

# 3. Set breakpoints and debug
```

#### Testing Production Build Locally

```bash
# 1. Build and start production mode
docker-compose -f docker-compose.yml up --build

# 2. Test at http://localhost

# 3. When done, stop containers
docker-compose down
```

### Troubleshooting

#### Hot Reload Not Working

**Symptom**: Code changes don't reflect in the browser

**Solution**:
1. Make sure you're running in development mode: `docker-compose up`
2. Check that source code volumes are mounted correctly
3. For frontend, verify `WATCHPACK_POLLING: "true"` is set
4. Rebuild the container: `docker-compose build frontend && docker-compose up`

#### API Calls Failing in Browser

**Symptom**: Frontend cannot reach backend API

**In Development Mode**:
- Frontend should use `http://localhost:8080`
- Verify `NEXT_PUBLIC_API_URL: http://localhost:8080` in docker-compose.override.yml
- Check backend is accessible at http://localhost:8080/api/hello

**In Production Mode**:
- Access via nginx at http://localhost (port 80)
- Frontend uses relative URLs (`/api`)
- Don't access frontend directly at :3000 (port not exposed)
- Verify nginx.conf routing is correct

#### Permission Errors

**Symptom**: EACCES or permission denied errors

**Solution**:
- Development mode runs as root, should not have permission issues
- Production mode runs as non-root user (nextjs)
- If files are created in development mode and then switched to production, ownership might be wrong
- Fix: `sudo chown -R $(id -u):$(id -g) frontend/` (on host)

#### Port Already in Use

**Symptom**: Cannot start containers, port 3000/8080/80 already in use

**Solution**:
```bash
# Find process using the port
lsof -i :8080
lsof -i :3000
lsof -i :80

# Kill the process or stop other Docker containers
docker-compose down

# Or modify ports in docker-compose.yml
```

#### Stale Data After Rebuild

**Symptom**: Old data or cache persists after rebuilding

**Solution**:
```bash
# Remove containers and volumes
docker-compose down -v

# Rebuild from scratch
docker-compose build --no-cache

# Start fresh
docker-compose up
```

#### Which Mode Am I Running?

**Check**:
```bash
# List running containers
docker-compose ps

# If you see source code mounts → Development mode
# If no mounts → Production mode

# Check frontend command
docker-compose ps frontend
# "pnpm dev" → Development
# "pnpm start" → Production
```

### Related Documentation

- Issue #65: Production deployment strategy
- nginx.conf: Reverse proxy configuration
- frontend/Dockerfile: Multi-stage build configuration
- backend/Dockerfile: Backend build configuration
## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master