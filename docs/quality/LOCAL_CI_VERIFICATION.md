# Local CI Verification

This guide explains how to run comprehensive CI checks locally before pushing to ensure your code will pass GitHub Actions CI.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Available Scripts](#available-scripts)
3. [Verification Levels](#verification-levels)
4. [Common Workflows](#common-workflows)
5. [Troubleshooting](#troubleshooting)
6. [CI vs Local Differences](#ci-vs-local-differences)
7. [Tips for Faster Checks](#tips-for-faster-checks)
8. [IDE Integration](#ide-integration)

## Quick Start

### 30-Second Guide

```bash
# Run all checks (recommended before creating a PR)
./scripts/ci-check-local.sh

# If all checks pass, you're ready to push!
git push
```

### What Gets Checked?

#### Backend
- ✅ Build and tests (Maven verify)
- ✅ Code coverage (≥80% lines, ≥75% branches)
- ✅ SpotBugs static analysis
- ✅ Checkstyle validation
- ✅ OWASP dependency-check (optional)

#### Frontend
- ✅ ESLint and Prettier
- ✅ TypeScript type checking
- ✅ Vitest tests with coverage (≥80% statements/functions/lines, ≥70% branches)
- ✅ Production build
- ✅ Playwright E2E tests (optional)

## Available Scripts

### Main Orchestration Script

**Location:** `scripts/ci-check-local.sh`

**Purpose:** Runs all CI checks in the correct order with clear output.

**Usage:**
```bash
./scripts/ci-check-local.sh [OPTIONS]
```

**Options:**
- `--backend-only` - Run only backend checks
- `--frontend-only` - Run only frontend checks
- `--e2e` - Include E2E tests (slow, ~3-5 minutes)
- `--dependency-check` - Include OWASP dependency-check (slow, ~5-10 minutes)
- `--parallel` - Run frontend lint and type-check in parallel
- `--verbose` - Show detailed output
- `--help` - Show help message

**Examples:**
```bash
# Standard check (2-3 minutes)
./scripts/ci-check-local.sh

# Quick backend only (1-2 minutes)
./scripts/ci-check-local.sh --backend-only

# Quick frontend only (1-2 minutes)
./scripts/ci-check-local.sh --frontend-only

# Full verification with E2E (8-10 minutes)
./scripts/ci-check-local.sh --e2e

# Comprehensive check with everything (10-15 minutes)
./scripts/ci-check-local.sh --e2e --dependency-check

# Fast parallel execution (1-2 minutes)
./scripts/ci-check-local.sh --parallel
```

**Exit Codes:**
- `0` - All checks passed
- `1-5` - Backend checks failed
- `11-40` - Frontend checks failed
- `99` - Environment validation failed

### Backend Verification Script

**Location:** `scripts/backend/ci-verify.sh`

**Purpose:** Replicates `.github/workflows/backend-ci.yml` exactly.

**Usage:**
```bash
./scripts/backend/ci-verify.sh [OPTIONS]
```

**Options:**
- `--dependency-check` - Run OWASP dependency-check
- `--verbose` - Show detailed Maven output
- `--help` - Show help message

**What it checks:**
1. Java 21 version
2. PostgreSQL availability (auto-starts if needed)
3. Maven build and verify (`./mvnw clean verify`)
4. JaCoCo coverage validation (parses XML report)
5. SpotBugs analysis (parses XML report)
6. Checkstyle validation (parses XML report)
7. OWASP dependency-check (optional, parses JSON report)

**Exit Codes:**
- `0` - All checks passed
- `1` - Build/test failure
- `2` - Coverage below threshold
- `3` - SpotBugs violations
- `4` - Checkstyle violations
- `5` - Dependency vulnerabilities
- `99` - Environment validation failed

### Frontend Verification Script

**Location:** `scripts/frontend/ci-verify.sh`

**Purpose:** Replicates `.github/workflows/frontend-ci.yml` exactly.

**Usage:**
```bash
./scripts/frontend/ci-verify.sh [OPTIONS]
```

**Options:**
- `--e2e` - Run E2E tests with Playwright
- `--parallel` - Run lint and type-check in parallel
- `--skip-install` - Skip dependency installation (faster iteration)
- `--verbose` - Show detailed output
- `--help` - Show help message

**What it checks:**
1. Node.js 20 and pnpm availability
2. Dependency installation (`pnpm install --frozen-lockfile`)
3. API client generation (`pnpm generate:api`)
4. ESLint (`pnpm lint`)
5. Prettier (`pnpm format:check`)
6. TypeScript type checking (`pnpm type-check`)
7. Vitest tests with coverage (`pnpm test:coverage`)
8. Coverage validation (parses JSON report)
9. Production build (`pnpm build`)
10. E2E tests (optional, uses Docker Compose for backend)

**Exit Codes:**
- `0` - All checks passed
- `11` - ESLint failure
- `12` - Prettier failure
- `13` - TypeScript error
- `21` - Coverage below threshold
- `30` - Build failure
- `40` - E2E test failure
- `99` - Environment validation failed

### Pre-push Hook

**Location:** `.husky/pre-push`

**Purpose:** Quick smoke tests before push (<30 seconds).

**What it does:**
- Detects changed files (backend/frontend)
- Backend: Runs `./mvnw compile test`
- Frontend: Runs `pnpm lint && pnpm type-check`
- Suggests running full verification

**To skip:**
```bash
git push --no-verify
```

## Verification Levels

### Level 1: Pre-commit Hook (~10s)

**When:** Every commit

**What:** Staged files only
- Prettier formatting
- ESLint on changed files
- TypeScript on changed files

**Trigger:** Automatic on `git commit`

**Skip:** `git commit --no-verify`

### Level 2: Pre-push Hook (~30s)

**When:** Before every push

**What:** Quick smoke tests
- Backend: `./mvnw compile test`
- Frontend: `pnpm lint && pnpm type-check`

**Trigger:** Automatic on `git push`

**Skip:** `git push --no-verify`

### Level 3: Full CI Verification (2-15min)

**When:** Before creating a PR or after fixing CI failures

**What:** Complete CI simulation
- All checks from Level 1 and 2
- Full test suite with coverage
- Quality tools (SpotBugs, Checkstyle)
- Production build
- Optional: E2E tests, dependency-check

**Trigger:** Manual
```bash
./scripts/ci-check-local.sh
```

**Cannot skip:** This is your safety net!

## Common Workflows

### Daily Development (Quick Iteration)

You're actively developing and want fast feedback:

```bash
# Option 1: Let pre-commit and pre-push hooks do their job
git add .
git commit -m "Work in progress"
git push  # Pre-push hook runs automatically

# Option 2: Test specific part manually
./scripts/ci-check-local.sh --backend-only  # or --frontend-only
```

### Before Creating a PR (Comprehensive Check)

You're ready to create a pull request:

```bash
# Standard check (no E2E, usually sufficient)
./scripts/ci-check-local.sh

# If you modified E2E tests or critical user flows
./scripts/ci-check-local.sh --e2e

# After checks pass
git push
# Create PR on GitHub
```

### After CI Failure (Debug Specific Issue)

GitHub Actions failed, and you need to reproduce locally:

```bash
# If backend CI failed
./scripts/backend/ci-verify.sh --verbose

# If frontend CI failed
./scripts/frontend/ci-verify.sh --verbose

# If E2E tests failed
./scripts/frontend/ci-verify.sh --e2e --verbose

# If dependency-check failed
./scripts/backend/ci-verify.sh --dependency-check --verbose
```

### Emergency Push (Skip Hooks)

You need to push immediately and will fix issues later:

```bash
git push --no-verify

# Don't forget to fix issues and re-push!
```

### Testing Specific Changes

You changed only backend or only frontend:

```bash
# Backend only (faster)
./scripts/ci-check-local.sh --backend-only

# Frontend only (faster)
./scripts/ci-check-local.sh --frontend-only
```

### Maximum Speed (Parallel Execution)

You want the fastest possible feedback:

```bash
# Parallel lint and type-check
./scripts/ci-check-local.sh --parallel

# Skip dependency installation if you haven't changed package.json
cd frontend && ../scripts/frontend/ci-verify.sh --skip-install
```

## Troubleshooting

### PostgreSQL Issues

**Problem:** Backend checks fail with database connection errors.

**Solution 1:** Auto-start via docker-compose
```bash
# The script will try this automatically, but you can do it manually:
docker-compose up -d postgres

# Verify PostgreSQL is running
docker-compose ps
```

**Solution 2:** Use existing PostgreSQL
```bash
# Ensure PostgreSQL is running with correct credentials
PGPASSWORD=testpass psql -h localhost -U testuser -d testdb -c "SELECT 1"

# If database doesn't exist, create it
PGPASSWORD=sandbox psql -h localhost -U sandbox -d postgres -c "CREATE DATABASE testdb;"
PGPASSWORD=sandbox psql -h localhost -U sandbox -d postgres -c "CREATE USER testuser WITH PASSWORD 'testpass';"
PGPASSWORD=sandbox psql -h localhost -U sandbox -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE testdb TO testuser;"
```

### Java Version Mismatch

**Problem:** `Java 21 required, but Java XX detected`

**Solution:** Use SDKMAN or switch Java version
```bash
# Using SDKMAN
sdk install java 21-tem
sdk use java 21-tem

# Or set JAVA_HOME manually
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Node Version Mismatch

**Problem:** `Node.js 20 recommended, but Node XX detected`

**Solution:** Use nvm to switch Node version
```bash
# Install nvm if not already installed
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash

# Install and use Node 20
nvm install 20
nvm use 20

# Make Node 20 default
nvm alias default 20
```

### Coverage Below Threshold

**Problem:** `Coverage below threshold` error

**Backend:**
```bash
# View detailed coverage report
open backend/target/site/jacoco/index.html

# Check which files have low coverage
grep -A 5 "INSUFFICIENTCOVERAGE" backend/target/site/jacoco/jacoco.xml

# Thresholds:
# - Lines: 80%
# - Branches: 75%
```

**Frontend:**
```bash
# View detailed coverage report
open frontend/coverage/index.html

# Check coverage summary
cat frontend/coverage/coverage-summary.json | jq '.total'

# Thresholds:
# - Statements: 80%
# - Branches: 70%
# - Functions: 80%
# - Lines: 80%
```

### SpotBugs Violations

**Problem:** SpotBugs reports violations

**Solution:**
```bash
# View detailed report
open backend/target/spotbugs.html

# Or check XML report
cat backend/target/spotbugsXml.xml

# To suppress false positives, edit:
vi backend/spotbugs-exclude.xml
```

### Checkstyle Violations

**Problem:** Checkstyle reports violations

**Solution:**
```bash
# View detailed report
cat backend/target/checkstyle-result.xml

# Most common issues:
# - Line too long (>100 characters)
# - Missing Javadoc
# - Import order

# Auto-fix some issues in IntelliJ IDEA:
# Code → Reformat Code
# Code → Optimize Imports
```

### Prettier Formatting Issues

**Problem:** `Prettier check failed`

**Solution:**
```bash
# Auto-fix all formatting issues
cd frontend
pnpm format

# Check what will be formatted
pnpm format:check

# Then commit the changes
git add .
git commit -m "Fix formatting"
```

### ESLint Errors

**Problem:** ESLint reports errors

**Solution:**
```bash
# View detailed errors
cd frontend
pnpm lint

# Auto-fix some issues
pnpm lint:fix

# For issues that can't be auto-fixed, edit the files manually
```

### TypeScript Errors

**Problem:** Type-check fails

**Solution:**
```bash
# View detailed errors
cd frontend
pnpm type-check

# Common issues:
# - Missing type definitions: `pnpm add -D @types/xxx`
# - Type mismatch: Check the error message and fix the type
# - Strict null checks: Add proper null checks or optional chaining
```

### Build Failures

**Problem:** Production build fails

**Frontend:**
```bash
# View full build output
cd frontend
pnpm build --verbose

# Common issues:
# - Environment variables not set
# - TypeScript errors (type-check first)
# - Import errors (check file paths)
# - Bundle size too large
```

**Backend:**
```bash
# View full Maven output
cd backend
./mvnw clean package -X  # -X for debug output

# Common issues:
# - Test failures (run tests first)
# - Compilation errors
# - Plugin configuration issues
```

### E2E Test Failures

**Problem:** E2E tests fail

**Solution:**
```bash
# Run with UI to see what's happening
cd frontend
pnpm exec playwright test --ui

# View test report
pnpm exec playwright show-report

# Check backend logs
docker-compose -f ../docker-compose.yml \
  -f ../docker-compose.override.yml \
  -f ../docker-compose.tmpfs.yml \
  -f ../docker-compose.e2e.yml \
  --env-file ../.env.e2e.example logs

# Common issues:
# - Backend not ready: Increase wait time
# - Selectors changed: Update test selectors
# - Flaky tests: Add proper waits, avoid timeouts
```

### Docker Issues

**Problem:** E2E tests fail with Docker errors

**Solution:**
```bash
# Check Docker is running
docker ps

# For macOS with Colima
colima start

# Clean up Docker resources
docker-compose down -v
docker system prune -a

# Rebuild images
docker-compose build --no-cache
```

### Slow Execution

**Problem:** Scripts take too long to run

**Solutions:**
```bash
# Use parallel execution
./scripts/ci-check-local.sh --parallel

# Skip dependency installation if nothing changed
cd frontend
../scripts/frontend/ci-verify.sh --skip-install

# Run only what changed
./scripts/ci-check-local.sh --backend-only  # or --frontend-only

# Skip slow optional checks
# Don't use --e2e unless you changed E2E tests
# Don't use --dependency-check unless you changed dependencies
```

## CI vs Local Differences

### Environment Comparison

| Aspect | GitHub CI | Local |
|--------|-----------|-------|
| OS | Ubuntu Latest | macOS (Darwin 25.2.0) |
| Java | 21 (Temurin) | 21 (varies) |
| Node | 20 | 20 (varies) |
| PostgreSQL | Docker service | Docker Compose or local install |
| Isolation | Fresh environment | May have cached state |
| Caching | GitHub Actions cache | Maven local cache, pnpm store |

### Key Differences

#### 1. PostgreSQL Setup

**CI:**
- Uses GitHub Actions service
- Fresh database on every run
- Credentials: testdb/testuser/testpass

**Local:**
- Uses docker-compose or local PostgreSQL
- Database persists between runs
- Credentials configurable
- Script auto-starts if needed

#### 2. Caching

**CI:**
- Maven: `~/.m2/repository` cached
- pnpm: Cached by package manager
- Fresh on new runners

**Local:**
- Maven: Cached locally
- pnpm: Cached locally
- May accumulate stale cache

**To clear local cache:**
```bash
# Maven
rm -rf ~/.m2/repository

# pnpm
pnpm store prune
```

#### 3. File Permissions

**CI:**
- All files have consistent permissions

**Local:**
- File permissions may vary
- Scripts must be executable

**To fix:**
```bash
chmod +x scripts/**/*.sh
chmod +x .husky/*
```

#### 4. Environment Variables

**CI:**
- Set in workflow files
- Secrets managed by GitHub

**Local:**
- Set in shell or .env files
- No secrets needed for basic checks

#### 5. Build Artifacts

**CI:**
- Uploaded to GitHub Actions artifacts
- Automatically cleaned up

**Local:**
- Remain in target/.next directories
- Must clean manually

**To clean:**
```bash
# Backend
cd backend && ./mvnw clean

# Frontend
cd frontend && rm -rf .next coverage
```

## Tips for Faster Checks

### 1. Use Focused Checks

```bash
# Only run what you changed
./scripts/ci-check-local.sh --backend-only
./scripts/ci-check-local.sh --frontend-only
```

### 2. Skip Optional Checks

```bash
# Don't run E2E unless necessary
./scripts/ci-check-local.sh  # No --e2e flag

# Don't run dependency-check unless dependencies changed
./scripts/ci-check-local.sh  # No --dependency-check flag
```

### 3. Use Parallel Execution

```bash
# Frontend: Run lint and type-check in parallel
./scripts/ci-check-local.sh --parallel
```

### 4. Skip Dependency Installation

```bash
# If package.json/pom.xml hasn't changed
cd frontend
../scripts/frontend/ci-verify.sh --skip-install
```

### 5. Run Tests Incrementally

```bash
# Backend: Run only changed tests
cd backend
./mvnw test -Dtest=MyChangedTest

# Frontend: Run only changed test files
cd frontend
pnpm test src/components/MyChangedComponent.test.tsx
```

### 6. Use IDE for Quick Checks

Before running full scripts:

**IntelliJ IDEA:**
- Run tests: Ctrl+Shift+F10
- Run SpotBugs: Analyze → Inspect Code
- Format code: Cmd+Opt+L

**VSCode:**
- Run tests: Test Explorer
- ESLint: Problems panel (auto-fixes available)
- Prettier: Format on Save

### 7. Watch Mode for Frontend

```bash
# Continuous test running during development
cd frontend
pnpm test --watch
```

### 8. Use Build Caches

```bash
# Maven: Keep local repository
# Don't run `mvn clean` unless necessary

# Frontend: Keep node_modules and .next cache
# Don't run `pnpm install` unless package.json changed
```

### Typical Execution Times

| Command | Time | When to Use |
|---------|------|-------------|
| Pre-push hook | ~30s | Every push (automatic) |
| `--backend-only` | ~2min | Backend changes only |
| `--frontend-only` | ~2min | Frontend changes only |
| Standard check | ~3min | Before PR (both changed) |
| `--parallel` | ~2min | Fast feedback |
| `--e2e` | ~8-10min | E2E tests changed |
| `--dependency-check` | ~10-15min | Dependencies changed |
| Everything | ~15-20min | Comprehensive check |

## IDE Integration

### IntelliJ IDEA

#### 1. Add as External Tool

1. Go to `Settings → Tools → External Tools`
2. Click `+` to add new tool
3. Configure:
   - Name: `CI Check`
   - Program: `$ProjectFileDir$/scripts/ci-check-local.sh`
   - Working directory: `$ProjectFileDir$`

#### 2. Add Keyboard Shortcut

1. Go to `Settings → Keymap`
2. Search for `CI Check`
3. Right-click → Add Keyboard Shortcut
4. Assign: `Ctrl+Shift+C` (or your preference)

#### 3. Add to Run Configurations

1. Go to `Run → Edit Configurations`
2. Click `+` → Shell Script
3. Configure:
   - Name: `CI Check`
   - Script path: `scripts/ci-check-local.sh`
   - Working directory: Project root

#### 4. Pre-commit Integration

IntelliJ IDEA automatically uses Git hooks in `.husky/`.

To disable temporarily:
- Uncheck `Settings → Version Control → Commit → Run Git hooks`

### Visual Studio Code

#### 1. Add as Task

Create `.vscode/tasks.json`:

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "CI Check",
      "type": "shell",
      "command": "./scripts/ci-check-local.sh",
      "problemMatcher": [],
      "group": {
        "kind": "build",
        "isDefault": true
      }
    },
    {
      "label": "CI Check (Backend Only)",
      "type": "shell",
      "command": "./scripts/ci-check-local.sh --backend-only",
      "problemMatcher": []
    },
    {
      "label": "CI Check (Frontend Only)",
      "type": "shell",
      "command": "./scripts/ci-check-local.sh --frontend-only",
      "problemMatcher": []
    }
  ]
}
```

#### 2. Run Tasks

- Open Command Palette: `Cmd+Shift+P`
- Type: `Tasks: Run Task`
- Select: `CI Check`

Or use keyboard shortcut: `Cmd+Shift+B` (default build task)

#### 3. Add Keyboard Shortcut

Create `.vscode/keybindings.json`:

```json
[
  {
    "key": "ctrl+shift+c",
    "command": "workbench.action.tasks.runTask",
    "args": "CI Check"
  }
]
```

#### 4. Pre-commit Integration

VSCode automatically uses Git hooks in `.husky/`.

To disable temporarily:
- Add to `.git/hooks/` files: `exit 0` at the top

### Command Line Aliases

Add to your `~/.bashrc` or `~/.zshrc`:

```bash
# CI check aliases
alias ci-check='./scripts/ci-check-local.sh'
alias ci-backend='./scripts/ci-check-local.sh --backend-only'
alias ci-frontend='./scripts/ci-check-local.sh --frontend-only'
alias ci-fast='./scripts/ci-check-local.sh --parallel'
alias ci-full='./scripts/ci-check-local.sh --e2e --dependency-check'
```

Then use:
```bash
ci-check         # Standard check
ci-backend       # Backend only
ci-frontend      # Frontend only
ci-fast          # Parallel execution
ci-full          # Everything
```

## Best Practices

### 1. Run Full Check Before PR

Always run the full check before creating a pull request:

```bash
./scripts/ci-check-local.sh
```

If you made E2E changes:

```bash
./scripts/ci-check-local.sh --e2e
```

### 2. Let Pre-push Hook Guide You

Don't skip the pre-push hook unless it's an emergency:

```bash
git push  # Let the hook run
```

The hook will suggest running full checks if needed.

### 3. Fix Issues Locally, Not in CI

Don't use CI as your linter. Run checks locally:

```bash
# Before committing
git add .
./scripts/ci-check-local.sh

# If checks pass
git commit -m "Feature complete"
git push
```

### 4. Use Verbose Flag for Debugging

When troubleshooting CI failures:

```bash
./scripts/ci-check-local.sh --verbose
```

### 5. Clean Build for Production

Before deploying or releasing:

```bash
# Backend
cd backend && ./mvnw clean verify

# Frontend
cd frontend && rm -rf .next && pnpm build
```

### 6. Keep Dependencies Updated

Run dependency-check regularly:

```bash
./scripts/ci-check-local.sh --dependency-check
```

Update dependencies before they become vulnerabilities.

### 7. Monitor Build Times

If checks get too slow, optimize:

- Remove unnecessary tests
- Use focused test suites
- Parallelize where possible
- Check for slow queries or network calls in tests

### 8. Document CI Failures

If CI fails despite local checks passing:

1. Reproduce locally with `--verbose`
2. Check CI logs for environment differences
3. Update scripts to match CI exactly
4. Document in this file

### 9. Educate Your Team

Share this guide with your team:

- New developers: Read "Quick Start"
- Before first PR: Read "Common Workflows"
- After CI failure: Read "Troubleshooting"

### 10. Continuous Improvement

The scripts should evolve with the project:

- Add new checks as CI adds them
- Update thresholds to match CI
- Improve error messages based on feedback
- Add new troubleshooting sections as issues arise

## Golden Rule

**If `./scripts/ci-check-local.sh` passes locally, CI should pass.**

If this rule is ever violated:
1. Investigate the difference
2. Update the scripts to match CI exactly
3. Document the issue
4. Add a test case to prevent regression

This is a living document. Keep it updated as the CI configuration changes!
