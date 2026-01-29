# Project Overview

## Structure

```
sandbox-claude-code/
├── backend/     # Spring Boot API (Java 21, Maven)
├── frontend/    # Next.js App (TypeScript, pnpm)
├── docs/        # Documentation
├── scripts/     # Utility scripts
└── nginx.conf   # Reverse proxy config
```

## Tech Stack

### Backend
- Spring Boot 3.4.1, Java 21
- PostgreSQL 16, MyBatis, Flyway
- Clean Architecture

### Frontend
- Next.js 15+ (App Router)
- shadcn/ui, Tailwind CSS
- TanStack Query, React Hook Form + Zod
- Vitest, Playwright, Storybook 10+

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/messages | Get all messages |
| GET | /api/messages/{id} | Get by ID |
| POST | /api/messages | Create |
| PUT | /api/messages/{id} | Update |
| DELETE | /api/messages/{id} | Delete |

## Development

```bash
# Backend
cd backend && ./mvnw spring-boot:run

# Frontend
cd frontend && pnpm dev

# Docker (both)
docker compose up
```

## Documentation Index

- [Docker Deployment](../../docs/DOCKER_DEPLOYMENT.md)
- [Git Worktree](../../docs/GIT_WORKTREE.md)
- [Local CI](../../docs/LOCAL_CI_VERIFICATION.md)
- [Storybook](../../docs/STORYBOOK.md)
- [Orval API](../../docs/ORVAL_API_GENERATION.md)
