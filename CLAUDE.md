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

- `GET /api/messages` - Get all messages
- `GET /api/messages/{id}` - Get message by ID
- `POST /api/messages` - Create new message
- `PUT /api/messages/{id}` - Update message
- `DELETE /api/messages/{id}` - Delete message

## Development Environment

- **IDE**: IntelliJ IDEA
- **Java**: JDK 21

## Local CI Verification

To ensure your changes pass GitHub CI before pushing, use the local verification scripts.

### Quick Start

```bash
# Quick check (2-3 minutes, recommended before PR)
./scripts/ci-check-local.sh

# Full check with E2E tests (8-10 minutes)
./scripts/ci-check-local.sh --e2e

# Backend or frontend only (1-2 minutes)
./scripts/ci-check-local.sh --backend-only
./scripts/ci-check-local.sh --frontend-only
```

### What Gets Checked

#### Backend
- ✅ Build and tests (Maven verify)
- ✅ Code coverage (≥80% lines, ≥75% branches)
- ✅ SpotBugs static analysis
- ✅ Checkstyle validation

#### Frontend
- ✅ ESLint and Prettier
- ✅ TypeScript type checking
- ✅ Vitest tests with coverage (≥80% statements/functions/lines, ≥70% branches)
- ✅ Production build

### Three-Tier Verification System

1. **Pre-commit hook** (~10s) - Automatic on commit
   - Prettier, ESLint, TypeScript on staged files

2. **Pre-push hook** (~30s) - Automatic on push
   - Backend: `./mvnw compile test`
   - Frontend: `pnpm lint && pnpm type-check`

3. **Full CI verification** (2-15min) - Manual before PR
   - Complete simulation of GitHub Actions CI
   - Run: `./scripts/ci-check-local.sh`

### Common Commands

```bash
# Standard check (recommended before creating PR)
./scripts/ci-check-local.sh

# Backend changes only
./scripts/ci-check-local.sh --backend-only

# Frontend changes only
./scripts/ci-check-local.sh --frontend-only

# Fast parallel execution
./scripts/ci-check-local.sh --parallel

# Full verification with everything
./scripts/ci-check-local.sh --e2e --dependency-check
```

### Skip Hooks (Emergency Only)

```bash
# Skip pre-commit hook
git commit --no-verify

# Skip pre-push hook
git push --no-verify
```

**Golden Rule:** If `./scripts/ci-check-local.sh` passes locally, CI should pass.

See [docs/LOCAL_CI_VERIFICATION.md](docs/LOCAL_CI_VERIFICATION.md) for detailed documentation, troubleshooting, and best practices.

## Docker Deployment

### Overview

This project uses a two-tier Docker Compose configuration:
- `docker-compose.yml` - Base configuration (production setup)
- `docker-compose.override.yml` - Development overrides (automatically applied)

Docker Compose automatically merges these files when you run `docker-compose up`, giving you a development environment with hot reload. To test the production setup, explicitly specify only the base file.

### Development Mode (Default) - `docker compose up`

**When to use**: Daily development work

```bash
docker compose up
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

### Production-like Testing - `docker compose -f docker-compose.yml up`

**When to use**: Testing production Docker build locally before deployment

```bash
docker compose -f docker-compose.yml up
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

#### Customizing Environment Variables

**Using .env.local (Recommended)**:

Development mode automatically mounts `frontend/.env.local` if it exists, allowing you to customize environment variables without modifying docker-compose files.

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

**Environment Variable Precedence** (highest to lowest):

1. docker-compose.override.yml (highest)
2. .env.local (medium)
3. docker-compose.yml (lowest)

**Important Notes**:

- Variables in docker-compose.override.yml override .env.local
- To use .env.local for a variable, remove it from docker-compose.override.yml
- `NEXT_PUBLIC_*` variables are bundled at build time (require rebuild)
- Other variables take effect on container restart
- .env.local is gitignored (safe for secrets in local development)

**Example**:

If you want to test a different API URL:

```bash
# Option 1: Remove NEXT_PUBLIC_API_URL from docker-compose.override.yml
# Then set it in .env.local
echo "NEXT_PUBLIC_API_URL=http://different-api:8080" >> frontend/.env.local
docker-compose build frontend && docker-compose up

# Option 2: Keep it in docker-compose.override.yml for team-wide default
# Individual developers can override by editing docker-compose.override.yml locally (not recommended)
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
- Check backend is accessible at http://localhost:8080/api/messages

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

## Git Worktree Support

### Overview

This project supports running multiple development environments simultaneously using git worktrees. This is useful when:

- Working on multiple features/branches in parallel
- Reviewing PRs while keeping your main development environment running
- Testing different configurations side-by-side
- Running integration tests without stopping your dev environment

The Docker Compose configuration uses environment variables to avoid naming conflicts and port collisions, allowing each worktree to have its own isolated environment.

### Quick Start

**Step 1: Create a new worktree**

```bash
# Create a worktree for a new branch
git worktree add ../sandbox-feature-auth feature/auth

# Or create a worktree from an existing branch
git worktree add ../sandbox-bugfix-123 bugfix/123
```

**Step 2: Set up the environment**

```bash
cd ../sandbox-feature-auth
./scripts/setup-worktree-env.sh
```

The script will:
- Auto-generate a project name from your branch name
- Suggest an available port offset (100, 200, 300, etc.)
- Create `.env` and `frontend/.env.local` files
- Display access URLs for your environment

**Step 3: Start the environment**

```bash
docker-compose up
```

Your new environment will be accessible at:
- Frontend: http://localhost:3100 (with offset=100)
- Backend: http://localhost:8180
- And so on...

**Step 4: When done**

```bash
# Stop and remove containers
docker-compose down

# Optional: Remove volumes to clean up database
docker-compose down -v

# Return to main worktree and remove the worktree
cd /Users/nagashimat/IdeaProjects/sandbox-claude-code
git worktree remove ../sandbox-feature-auth
```

### How It Works

#### COMPOSE_PROJECT_NAME

Docker Compose uses `COMPOSE_PROJECT_NAME` to namespace all resources:

- **Containers**: `${COMPOSE_PROJECT_NAME}-backend-1`, `${COMPOSE_PROJECT_NAME}-frontend-1`, etc.
- **Volumes**: `${COMPOSE_PROJECT_NAME}_postgres-data`
- **Networks**: `${COMPOSE_PROJECT_NAME}_default`

Each worktree gets a unique project name, preventing naming conflicts.

#### PORT_OFFSET

To avoid port collisions, each environment uses a different port offset:

```
Actual Port = Base Port + PORT_OFFSET
```

**Base Ports**:
- Frontend: 3000
- Backend: 8080
- PostgreSQL: 5432
- Debug: 5005
- Nginx: 80

**Example with PORT_OFFSET=100**:
- Frontend: 3100
- Backend: 8180
- PostgreSQL: 5532
- Debug: 5105
- Nginx: 180

The setup script automatically calculates these ports and writes them to `.env`.

#### Database Isolation

Each worktree has its own PostgreSQL database:

- **Volume Mode** (default): Data persists in `${COMPOSE_PROJECT_NAME}_postgres-data` volume
- **Tmpfs Mode** (optional): Data stored in memory, faster but lost on restart

This ensures complete data isolation between environments.

### Port Allocation Table

| Offset | Frontend | Backend | PostgreSQL | Debug | Nginx | Recommended Use |
|--------|----------|---------|------------|-------|-------|-----------------|
| 0      | 3000     | 8080    | 5432       | 5005  | 80    | Main development (sandbox-dev) |
| 1      | 3001     | 8081    | 5433       | 5006  | 81    | E2E testing (sandbox-e2e) |
| 100    | 3100     | 8180    | 5532       | 5105  | 180   | Feature branch 1 |
| 200    | 3200     | 8280    | 5632       | 5205  | 280   | Feature branch 2 |
| 300    | 3300     | 8380    | 5732       | 5305  | 380   | Feature branch 3 |

### Manual Setup (Alternative to Script)

If you prefer to configure manually or need to customize:

**Step 1: Create `.env` file**

```bash
cp .env.example .env
```

**Step 2: Edit `.env`**

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

**Step 3: Create `frontend/.env.local`**

```bash
cat > frontend/.env.local <<EOF
# Frontend Environment Configuration
NEXT_PUBLIC_API_URL=http://localhost:8180
EOF
```

**Step 4: Start environment**

```bash
docker-compose up
```

### Common Commands

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

### E2E Testing with Isolated Environment

E2E tests can run in their own isolated environment:

**Step 1: Create E2E environment file**

```bash
cp .env.e2e.example .env.e2e
```

**Step 2: Run E2E tests**

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

### Troubleshooting

#### Port Already in Use

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

#### Container Name Conflicts

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

#### Frontend Can't Connect to Backend

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

#### Database Data Mixing

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

#### Which Environment Am I In?

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

#### Wrong Ports After Changing Offset

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

### Best Practices

#### 1. Use Descriptive Project Names

```bash
# Good
COMPOSE_PROJECT_NAME=sandbox-feature-auth
COMPOSE_PROJECT_NAME=sandbox-bugfix-user-login
COMPOSE_PROJECT_NAME=sandbox-pr-123

# Avoid
COMPOSE_PROJECT_NAME=test
COMPOSE_PROJECT_NAME=dev
```

#### 2. Standard Offset Values

Use multiples of 100 for easier mental mapping:

```
0   - Main development
1   - E2E testing
100 - Feature branch 1
200 - Feature branch 2
...
```

#### 3. Clean Up When Done

```bash
# Stop containers and remove volumes
docker-compose down -v

# Remove worktree
git worktree remove ../sandbox-feature-name

# This prevents disk space issues from accumulating volumes
```

#### 4. Document Your Offsets

Keep a note of which offsets you're using:

```bash
# Check all environments
./scripts/check-ports.sh

# Or list all volumes
docker volume ls | grep sandbox
```

#### 5. Use Tmpfs for Short-Lived Environments

For quick tests or PR reviews:

```bash
# Set in .env
DB_PERSISTENCE_MODE=tmpfs

# Faster startup, no cleanup needed
```

#### 6. One Main Environment at Offset 0

Keep your primary development at offset 0:
- Familiar ports (3000, 8080, etc.)
- Easier to remember
- Default if you forget to set offset

#### 7. Verify Setup Before Starting

```bash
# After running setup script, verify:
cat .env
cat frontend/.env.local
./scripts/check-ports.sh

# Then start
docker-compose up
```

### File Structure

```
/Users/nagashimat/IdeaProjects/sandbox-claude-code/
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

### Environment Variables Reference

**.env**:
- `COMPOSE_PROJECT_NAME`: Unique project name for namespacing
- `PORT_OFFSET`: Base offset for all ports
- `FRONTEND_PORT`: Calculated frontend port
- `BACKEND_PORT`: Calculated backend port
- `POSTGRES_PORT`: Calculated PostgreSQL port
- `DEBUG_PORT`: Calculated debug port
- `NGINX_PORT`: Calculated nginx port
- `POSTGRES_DB`: Database name
- `POSTGRES_USER`: Database user
- `POSTGRES_PASSWORD`: Database password
- `DB_PERSISTENCE_MODE`: `volume` or `tmpfs`

**frontend/.env.local**:
- `NEXT_PUBLIC_API_URL`: Backend API URL for browser (build-time)

## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master