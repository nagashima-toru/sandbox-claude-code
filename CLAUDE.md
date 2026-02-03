# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a sandbox repository for experimenting with Claude Code.

## Project Structure

```
sandbox-claude-code/
├── backend/     # Spring Boot API (Java 25, Maven)
├── frontend/    # Next.js App (TypeScript, pnpm)
├── docs/        # Documentation
├── scripts/     # Utility scripts
└── nginx.conf   # Reverse proxy configuration
```

## Quick Start

### Local Development (without Docker)

```bash
# Backend (port 8080)
cd backend && ./mvnw spring-boot:run

# Frontend (port 3000)
cd frontend && pnpm install && pnpm dev
```

### Docker Development

```bash
# Development mode (hot reload)
docker compose up

# Production mode test
docker compose -f docker-compose.yml up
```

## Development Environment

- **IDE**: IntelliJ IDEA
- **Java**: JDK 25
- **Node.js**: See frontend/.nvmrc
- **Package Manager**: pnpm (frontend), Maven (backend)

## 開発作業全体のプロセス

1. 要求仕様の理解
2. 現在の実装調査
3. 実装計画策定
4. 計画レビュー
5. 実装/単体テスト実施
6. 実装/単体テスト review 実施 & 指摘修正
7. 結合テスト実施
8. 結合テスト review 実施 & 指摘修正
9. deploy 前確認

## Code Formatting

### Automatic Formatting (Claude Code)

Claude Code automatically formats code after editing files:
- **Backend** (Java): Spotless with Google Java Format
- **Frontend** (TypeScript/JavaScript): Prettier + ESLint

Configuration: `.claude/settings.local.json` hooks run `./scripts/format-code.sh` after `Edit` and `Write` operations.

### Manual Formatting

```bash
# Backend
cd backend && ./mvnw spotless:apply

# Frontend
cd frontend && pnpm format
```

## Development Workflows

### After Backend API Changes

```bash
# 1. Generate OpenAPI spec
cd backend && ./mvnw verify

# 2. Regenerate frontend API client
cd frontend && pnpm generate:api

# 3. TypeScript will show type errors if contract changed
```

### Adding npm Packages (Docker)

```bash
# With running container
docker exec sandbox-frontend pnpm add <package>

# Or rebuild
cd frontend && pnpm add <package>
docker compose build frontend
```

### Creating a New Component

1. Create component in `frontend/src/components/`
2. Create story: `ComponentName.stories.tsx`
3. Create test: `ComponentName.test.tsx`
4. Develop in Storybook: `pnpm storybook`

## Docker Quick Reference

| Mode | Command | Access |
|------|---------|--------|
| Development | `docker compose up` | localhost:3000 (frontend), localhost:8080 (backend) |
| Production | `docker compose -f docker-compose.yml up` | localhost:80 (nginx) |

**Key features**:
- Development: Hot reload, debug port (5005)
- Production: Nginx reverse proxy, optimized builds

See [docs/DOCKER_DEPLOYMENT.md](docs/DOCKER_DEPLOYMENT.md) for details.

## Git Worktree Quick Reference

Run multiple environments simultaneously:

```bash
# Create worktree
git worktree add ../sandbox-feature feature/name

# Setup environment
cd ../sandbox-feature && ./scripts/setup-worktree-env.sh

# Start (uses different ports)
docker compose up
```

See [docs/GIT_WORKTREE.md](docs/GIT_WORKTREE.md) for details.

## Local CI Verification

```bash
# Standard check (before PR)
./scripts/ci-check-local.sh

# Backend only
./scripts/ci-check-local.sh --backend-only

# Frontend only
./scripts/ci-check-local.sh --frontend-only

# Full with E2E
./scripts/ci-check-local.sh --e2e
```

**Coverage Requirements**:
- Backend: ≥80% lines, ≥75% branches
- Frontend: ≥80% statements/functions/lines, ≥70% branches

**Golden Rule**: If `./scripts/ci-check-local.sh` passes locally, CI should pass.

See [docs/LOCAL_CI_VERIFICATION.md](docs/LOCAL_CI_VERIFICATION.md) for details.

## Key Conventions

### Code Style

- **Backend**: Clean Architecture, JUnit tests for all classes
- **Frontend**: Functional components, named exports, co-located tests/stories

### File Naming

| Type | Convention | Example |
|------|------------|---------|
| React Component | PascalCase | `MessageTable.tsx` |
| Hook | camelCase + use | `useMessages.ts` |
| Story | + .stories | `MessageTable.stories.tsx` |
| Test | + .test | `MessageTable.test.tsx` |

### Git

- Branch: `feature/`, `bugfix/`, `hotfix/`
- Run CI check before PR: `./scripts/ci-check-local.sh`

## Documentation Index

| Document | Description |
|----------|-------------|
| [Docker Deployment](docs/DOCKER_DEPLOYMENT.md) | Docker dev/prod modes, workflows, troubleshooting |
| [Git Worktree](docs/GIT_WORKTREE.md) | Multi-environment development |
| [Local CI Verification](docs/LOCAL_CI_VERIFICATION.md) | CI checks, coverage, hooks |
| [Storybook](docs/STORYBOOK.md) | Component development, MSW, a11y |
| [Orval API Generation](docs/ORVAL_API_GENERATION.md) | API client generation |
| [Test Strategy](backend/docs/TEST_STRATEGY.md) | Backend testing guidelines |
| [Security](docs/SECURITY.md) | Security guidelines |

### Subdirectory Documentation

- [backend/CLAUDE.md](backend/CLAUDE.md) - Backend-specific guidance
- [frontend/CLAUDE.md](frontend/CLAUDE.md) - Frontend-specific guidance

## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master
