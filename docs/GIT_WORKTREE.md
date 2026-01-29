# Git Worktree Support

This document provides detailed guidance for running multiple development environments simultaneously using git worktrees.

## Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [How It Works](#how-it-works)
- [Port Allocation](#port-allocation)
- [Manual Setup](#manual-setup)
- [Common Commands](#common-commands)
- [Database Configuration](#database-configuration)
- [E2E Testing](#e2e-testing)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)
- [Environment Variables Reference](#environment-variables-reference)
- [File Structure](#file-structure)

## Overview

This project supports running multiple development environments simultaneously using git worktrees. This is useful when:

- Working on multiple features/branches in parallel
- Reviewing PRs while keeping your main development environment running
- Testing different configurations side-by-side
- Running integration tests without stopping your dev environment

The Docker Compose configuration uses environment variables to avoid naming conflicts and port collisions, allowing each worktree to have its own isolated environment.

## Quick Start

### Step 1: Create a new worktree

```bash
# Create a worktree for a new branch
git worktree add ../sandbox-feature-auth feature/auth

# Or create a worktree from an existing branch
git worktree add ../sandbox-bugfix-123 bugfix/123
```

### Step 2: Set up the environment

```bash
cd ../sandbox-feature-auth
./scripts/setup-worktree-env.sh
```

The script will:
- Auto-generate a project name from your branch name
- Suggest an available port offset (100, 200, 300, etc.)
- Create `.env` and `frontend/.env.local` files
- Display access URLs for your environment

### Step 3: Start the environment

```bash
docker-compose up
```

Your new environment will be accessible at:
- Frontend: http://localhost:3100 (with offset=100)
- Backend: http://localhost:8180
- And so on...

### Step 4: When done

```bash
# Stop and remove containers
docker-compose down

# Optional: Remove volumes to clean up database
docker-compose down -v

# Return to main worktree and remove the worktree
cd /path/to/main/repo
git worktree remove ../sandbox-feature-auth
```

## How It Works

### COMPOSE_PROJECT_NAME

Docker Compose uses `COMPOSE_PROJECT_NAME` to namespace all resources:

- **Containers**: `${COMPOSE_PROJECT_NAME}-backend-1`, `${COMPOSE_PROJECT_NAME}-frontend-1`, etc.
- **Volumes**: `${COMPOSE_PROJECT_NAME}_postgres-data`
- **Networks**: `${COMPOSE_PROJECT_NAME}_default`

Each worktree gets a unique project name, preventing naming conflicts.

### PORT_OFFSET

To avoid port collisions, each environment uses a different port offset:

```
Actual Port = Base Port + PORT_OFFSET
```

**Base Ports**:
| Service | Base Port |
|---------|-----------|
| Frontend | 3000 |
| Backend | 8080 |
| PostgreSQL | 5432 |
| Debug | 5005 |
| Nginx | 80 |

**Example with PORT_OFFSET=100**:
| Service | Port |
|---------|------|
| Frontend | 3100 |
| Backend | 8180 |
| PostgreSQL | 5532 |
| Debug | 5105 |
| Nginx | 180 |

The setup script automatically calculates these ports and writes them to `.env`.

### Database Isolation

Each worktree has its own PostgreSQL database:

- **Volume Mode** (default): Data persists in `${COMPOSE_PROJECT_NAME}_postgres-data` volume
- **Tmpfs Mode** (optional): Data stored in memory, faster but lost on restart

This ensures complete data isolation between environments.

## Port Allocation

| Offset | Frontend | Backend | PostgreSQL | Debug | Nginx | Recommended Use |
|--------|----------|---------|------------|-------|-------|-----------------|
| 0 | 3000 | 8080 | 5432 | 5005 | 80 | Main development (sandbox-dev) |
| 1 | 3001 | 8081 | 5433 | 5006 | 81 | E2E testing (sandbox-e2e) |
| 100 | 3100 | 8180 | 5532 | 5105 | 180 | Feature branch 1 |
| 200 | 3200 | 8280 | 5632 | 5205 | 280 | Feature branch 2 |
| 300 | 3300 | 8380 | 5732 | 5305 | 380 | Feature branch 3 |

## Manual Setup

If you prefer to configure manually or need to customize:

### Step 1: Create `.env` file

```bash
cp .env.example .env
```

### Step 2: Edit `.env`

```bash
# Set unique project name
COMPOSE_PROJECT_NAME=sandbox-feature-auth

# Set port offset
PORT_OFFSET=100

# Calculated ports (automatically computed)
FRONTEND_PORT=3100
BACKEND_PORT=8180
POSTGRES_PORT=5532
DEBUG_PORT=5105
NGINX_PORT=180

# Database config
POSTGRES_DB=sandbox
POSTGRES_USER=sandbox
POSTGRES_PASSWORD=sandbox
DB_PERSISTENCE_MODE=volume
```

### Step 3: Create `frontend/.env.local`

```bash
cat > frontend/.env.local <<EOF
# Frontend Environment Configuration
NEXT_PUBLIC_API_URL=http://localhost:8180
EOF
```

### Step 4: Start environment

```bash
docker-compose up
```

## Common Commands

```bash
# Check running containers and port usage
./scripts/check-ports.sh

# Start environment
docker-compose up

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f

# Stop environment
docker-compose down

# Stop and remove volumes (clean database)
docker-compose down -v

# Rebuild after changing PORT_OFFSET
./scripts/setup-worktree-env.sh --regenerate
docker-compose build frontend
docker-compose up
```

## Database Configuration

### Using Tmpfs for Database

For faster testing or temporary environments:

**Option 1: Set in `.env`**

```bash
DB_PERSISTENCE_MODE=tmpfs
```

Then start normally:

```bash
docker-compose up
```

**Option 2: Use tmpfs compose file**

```bash
docker-compose -f docker-compose.yml \
  -f docker-compose.override.yml \
  -f docker-compose.tmpfs.yml up
```

**Benefits**:
- Faster database operations (in-memory)
- No disk I/O overhead
- Automatic cleanup

**Caution**:
- All data is lost when container stops
- Not suitable for development work requiring data persistence

## E2E Testing

E2E tests can run in their own isolated environment:

### Step 1: Create E2E environment file

```bash
cp .env.e2e.example .env.e2e
```

### Step 2: Run E2E tests

```bash
# Use .env.e2e configuration
docker-compose --env-file .env.e2e up

# Or use with E2E-specific overrides
docker-compose -f docker-compose.yml \
  -f docker-compose.override.yml \
  -f docker-compose.e2e.yml \
  -f docker-compose.tmpfs.yml \
  --env-file .env.e2e up
```

The E2E environment uses:
- `COMPOSE_PROJECT_NAME=sandbox-e2e`
- `PORT_OFFSET=1` (ports 3001, 8081, 5433, etc.)
- `DB_PERSISTENCE_MODE=tmpfs` (no persistent data)

## Best Practices

### 1. Use Descriptive Project Names

```bash
# Good
COMPOSE_PROJECT_NAME=sandbox-feature-auth
COMPOSE_PROJECT_NAME=sandbox-bugfix-user-login
COMPOSE_PROJECT_NAME=sandbox-pr-123

# Avoid
COMPOSE_PROJECT_NAME=test
COMPOSE_PROJECT_NAME=dev
```

### 2. Standard Offset Values

Use multiples of 100 for easier mental mapping:

```
0   - Main development
1   - E2E testing
100 - Feature branch 1
200 - Feature branch 2
...
```

### 3. Clean Up When Done

```bash
# Stop containers and remove volumes
docker-compose down -v

# Remove worktree
git worktree remove ../sandbox-feature-name

# This prevents disk space issues from accumulating volumes
```

### 4. Document Your Offsets

Keep a note of which offsets you're using:

```bash
# Check all environments
./scripts/check-ports.sh

# Or list all volumes
docker volume ls | grep sandbox
```

### 5. Use Tmpfs for Short-Lived Environments

For quick tests or PR reviews:

```bash
# Set in .env
DB_PERSISTENCE_MODE=tmpfs

# Faster startup, no cleanup needed
```

### 6. One Main Environment at Offset 0

Keep your primary development at offset 0:
- Familiar ports (3000, 8080, etc.)
- Easier to remember
- Default if you forget to set offset

### 7. Verify Setup Before Starting

```bash
# After running setup script, verify:
cat .env
cat frontend/.env.local
./scripts/check-ports.sh

# Then start
docker-compose up
```

## Troubleshooting

### Port Already in Use

**Problem**: `Error: bind: address already in use`

**Solution**:
```bash
# Check what's using the ports
./scripts/check-ports.sh

# Choose a different offset
./scripts/setup-worktree-env.sh
# Select next available offset when prompted

# Or manually edit .env
vim .env  # Change PORT_OFFSET to next available value
```

### Container Name Conflicts

**Problem**: `Error: container name already in use`

**Solution**:

This shouldn't happen if you're using unique `COMPOSE_PROJECT_NAME` values. Check:

```bash
# View .env file
cat .env | grep COMPOSE_PROJECT_NAME

# Ensure it's unique across all worktrees
# Change if needed
COMPOSE_PROJECT_NAME=sandbox-my-unique-name
```

### Frontend Can't Connect to Backend

**Problem**: Frontend shows API connection errors

**Solution**:

```bash
# Check frontend/.env.local has correct backend port
cat frontend/.env.local

# Should match BACKEND_PORT in .env
# If not, regenerate:
./scripts/setup-worktree-env.sh --regenerate

# Rebuild frontend (NEXT_PUBLIC_* vars are build-time)
docker-compose build frontend
docker-compose up
```

### Database Data Mixing

**Problem**: Data from different environments is mixing

**Solution**:

This shouldn't happen. Verify:

```bash
# Check volume names
docker volume ls | grep sandbox

# Each worktree should have its own volume
# Format: ${COMPOSE_PROJECT_NAME}_postgres-data

# If needed, remove specific volume
docker-compose down -v
```

### Which Environment Am I In?

**Problem**: Confused about which worktree/environment is running

**Solution**:

```bash
# Check current directory
pwd

# Check current branch
git branch --show-current

# Check .env project name
cat .env | grep COMPOSE_PROJECT_NAME

# Check running containers
docker-compose ps

# Check all sandbox containers
docker ps --filter "name=sandbox"
```

### Wrong Ports After Changing Offset

**Problem**: Changed PORT_OFFSET but ports haven't changed

**Solution**:

```bash
# Regenerate configuration
./scripts/setup-worktree-env.sh --regenerate

# Stop and remove old containers
docker-compose down

# Rebuild frontend (NEXT_PUBLIC_API_URL is build-time)
docker-compose build frontend

# Start with new ports
docker-compose up
```

## Environment Variables Reference

### .env File

| Variable | Description |
|----------|-------------|
| `COMPOSE_PROJECT_NAME` | Unique project name for namespacing |
| `PORT_OFFSET` | Base offset for all ports |
| `FRONTEND_PORT` | Calculated frontend port |
| `BACKEND_PORT` | Calculated backend port |
| `POSTGRES_PORT` | Calculated PostgreSQL port |
| `DEBUG_PORT` | Calculated debug port |
| `NGINX_PORT` | Calculated nginx port |
| `POSTGRES_DB` | Database name |
| `POSTGRES_USER` | Database user |
| `POSTGRES_PASSWORD` | Database password |
| `DB_PERSISTENCE_MODE` | `volume` or `tmpfs` |

### frontend/.env.local

| Variable | Description |
|----------|-------------|
| `NEXT_PUBLIC_API_URL` | Backend API URL for browser (build-time) |

## File Structure

```
project-root/
├── .env.example              # Template for environment variables
├── .env.e2e.example          # Template for E2E environment
├── .env                      # Your environment config (gitignored)
├── docker-compose.yml        # Base configuration
├── docker-compose.override.yml  # Development overrides
├── docker-compose.tmpfs.yml  # Tmpfs option for database
├── docker-compose.e2e.yml    # E2E-specific overrides
├── frontend/
│   └── .env.local            # Frontend config (gitignored, auto-generated)
└── scripts/
    ├── setup-worktree-env.sh # Interactive setup script
    └── check-ports.sh        # Port usage checker
```

## Related Documentation

- [Docker Deployment](DOCKER_DEPLOYMENT.md) - Docker development and production modes
- [Local CI Verification](LOCAL_CI_VERIFICATION.md) - CI checks before push
