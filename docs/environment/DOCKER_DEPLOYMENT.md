# Docker Deployment Guide

This document provides detailed guidance for Docker-based development and deployment.

## Table of Contents

- [Overview](#overview)
- [Development Mode](#development-mode)
- [Production Mode](#production-mode)
- [Architecture](#architecture)
- [Common Commands](#common-commands)
- [Workflows](#workflows)
- [Environment Variables](#environment-variables)
- [Troubleshooting](#troubleshooting)

## Overview

This project uses a two-tier Docker Compose configuration:

- `docker-compose.yml` - Base configuration (production setup)
- `docker-compose.override.yml` - Development overrides (automatically applied)

Docker Compose automatically merges these files when you run `docker-compose up`, giving you a development
environment with hot reload. To test the production setup, explicitly specify only the base file.

## Development Mode

**Command**: `docker compose up`

**When to use**: Daily development work

### Configuration

| Service | Port | Notes |
|---------|------|-------|
| Frontend | 3000 | Direct access, hot reload |
| Backend | 8080 | Direct access |
| Nginx | 80 | Optional, usually use :3000 |
| Debug | 5005 | Backend remote debugging |

### Features

- Automatically uses `docker-compose.yml` + `docker-compose.override.yml`
- Builds from Dockerfile `development` target
- Enables hot reload for both frontend and backend
- Mounts source code into containers for live updates
- Exposes debug port (5005) for backend debugging
- Frontend runs with `pnpm dev`
- Backend runs with Spring Boot DevTools
- Frontend connects to backend via `http://localhost:8080` (direct browser access)
- Runs as root user (for easier development)
- Node environment: development

### Pros and Cons

**Pros**:

- Fast feedback loop with hot reload
- Easy debugging
- No rebuild needed for code changes

**Cons**:

- Not representative of production environment
- Larger image size
- Development dependencies included

## Production Mode

**Command**: `docker compose -f docker-compose.yml up`

**When to use**: Testing production Docker build locally before deployment

### Configuration

| Service | Port | Notes |
|---------|------|-------|
| Access | 80 | Via Nginx only |
| Frontend | Internal 3000 | Not exposed externally |
| Backend | Internal 8080 | Not exposed externally |

### Features

- Uses only `docker-compose.yml` (no override file)
- Builds from Dockerfile `production` target
- Optimized production build
- No source code mounts
- No hot reload
- No debug ports
- Frontend runs with `pnpm start` (production server)
- Nginx reverse proxy routes: `/` → frontend:3000, `/api` → backend:8080
- Frontend connects to backend via `/api` (relative URL through nginx)
- Runs as non-root user (nextjs) for security
- Node environment: production

### Pros and Cons

**Pros**:

- Tests actual production configuration
- Smaller image size
- Security best practices (non-root user)
- Representative of production behavior

**Cons**:

- Requires rebuild for any code changes
- No hot reload
- Slower iteration cycle

## Architecture

### Why Nginx Reverse Proxy?

**Problem**: The frontend's `NEXT_PUBLIC_API_URL` is bundled into browser JavaScript during build time. Using Docker
internal hostnames like `http://backend:8080` doesn't work because external browsers cannot resolve them.

**Solution**: Nginx reverse proxy provides:

1. Single access point for users (<http://localhost>)
2. Frontend and backend on same domain (no CORS issues)
3. Relative URLs work (`/api` resolves to same domain)
4. Production-ready architecture
5. Easy SSL termination in the future

## Common Commands

```bash
# Development mode (with hot reload)
docker compose up

# Development mode in background
docker compose up -d

# Production-like mode (optimized build)
docker compose -f docker-compose.yml up

# Rebuild specific service (development mode)
docker compose build backend
docker compose build frontend

# Rebuild specific service (production mode)
docker compose -f docker-compose.yml build backend
docker compose -f docker-compose.yml build frontend

# View logs
docker compose logs -f
docker compose logs -f frontend
docker compose logs -f backend

# Stop and remove containers
docker compose down

# Stop and remove containers with volumes
docker compose down -v
```

## Workflows

### Adding a New npm Package

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

### Changing Dockerfile Configuration

```bash
# 1. Edit Dockerfile
# 2. Rebuild the affected service
docker-compose build <service-name>

# 3. Restart
docker-compose up
```

### Debugging Backend in IDE

```bash
# 1. Start in development mode
docker-compose up

# 2. Configure IntelliJ IDEA remote debugging:
#    - Host: localhost
#    - Port: 5005
#    - Debugger mode: Attach to remote JVM

# 3. Set breakpoints and debug
```

### Testing Production Build Locally

```bash
# 1. Build and start production mode
docker-compose -f docker-compose.yml up --build

# 2. Test at http://localhost

# 3. When done, stop containers
docker-compose down
```

## Environment Variables

### Using .env.local (Recommended)

Development mode automatically mounts `frontend/.env.local` if it exists, allowing you to customize environment
variables without modifying docker-compose files.

```bash
# 1. Copy example file
cp frontend/.env.local.example frontend/.env.local

# 2. Edit with your custom values
vim frontend/.env.local

# 3. Restart container (for runtime variables)
docker-compose restart frontend

# 4. For NEXT_PUBLIC_* variables (build-time), rebuild:
docker-compose build frontend && docker-compose up
```

### Precedence (highest to lowest)

1. docker-compose.override.yml (highest)
2. .env.local (medium)
3. docker-compose.yml (lowest)

### Important Notes

- Variables in docker-compose.override.yml override .env.local
- To use .env.local for a variable, remove it from docker-compose.override.yml
- `NEXT_PUBLIC_*` variables are bundled at build time (require rebuild)
- Other variables take effect on container restart
- .env.local is gitignored (safe for secrets in local development)

### Example: Testing a Different API URL

```bash
# Option 1: Remove NEXT_PUBLIC_API_URL from docker-compose.override.yml
# Then set it in .env.local
echo "NEXT_PUBLIC_API_URL=http://different-api:8080" >> frontend/.env.local
docker-compose build frontend && docker-compose up

# Option 2: Keep it in docker-compose.override.yml for team-wide default
# Individual developers can override by editing docker-compose.override.yml locally (not recommended)
```

## Troubleshooting

### Hot Reload Not Working

**Symptom**: Code changes don't reflect in the browser

**Solution**:

1. Make sure you're running in development mode: `docker-compose up`
2. Check that source code volumes are mounted correctly
3. For frontend, verify `WATCHPACK_POLLING: "true"` is set
4. Rebuild the container: `docker-compose build frontend && docker-compose up`

### API Calls Failing in Browser

**Symptom**: Frontend cannot reach backend API

**In Development Mode**:

- Frontend should use `http://localhost:8080`
- Verify `NEXT_PUBLIC_API_URL: http://localhost:8080` in docker-compose.override.yml
- Check backend is accessible at <http://localhost:8080/api/messages>

**In Production Mode**:

- Access via nginx at <http://localhost> (port 80)
- Frontend uses relative URLs (`/api`)
- Don't access frontend directly at :3000 (port not exposed)
- Verify nginx.conf routing is correct

### Permission Errors

**Symptom**: EACCES or permission denied errors

**Solution**:

- Development mode runs as root, should not have permission issues
- Production mode runs as non-root user (nextjs)
- If files are created in development mode and then switched to production, ownership might be wrong
- Fix: `sudo chown -R $(id -u):$(id -g) frontend/` (on host)

### Port Already in Use

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

### Stale Data After Rebuild

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

### Which Mode Am I Running?

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

## Related Documentation

- [Git Worktree Support](GIT_WORKTREE.md) - Multi-environment development
- [Local CI Verification](LOCAL_CI_VERIFICATION.md) - CI checks before push
- Issue #65: Production deployment strategy
- nginx.conf: Reverse proxy configuration
- frontend/Dockerfile: Multi-stage build configuration
- backend/Dockerfile: Backend build configuration
