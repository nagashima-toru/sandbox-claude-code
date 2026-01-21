# Sandbox Claude Code

[![Backend CI](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/backend-ci.yml)
[![CodeQL](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/codeql.yml/badge.svg)](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/codeql.yml)

A sandbox repository for experimenting with Claude Code.

## Project Structure

```
sandbox-claude-code/
├── backend/          # Spring Boot API application (Java 21, Maven)
├── frontend/         # Next.js application (TypeScript, pnpm)
├── docs/             # Project documentation
│   └── GITIGNORE_RULES.md
├── CLAUDE.md         # Root project guidance for Claude Code
├── .gitignore        # Common rules (IDE, OS)
├── backend/.gitignore    # Backend-specific rules
└── frontend/.gitignore   # Frontend-specific rules
```

## Components

### Backend

Spring Boot API application with PostgreSQL, MyBatis, and Flyway.

- **Tech Stack**: Java 21, Spring Boot 3.4.1, PostgreSQL, MyBatis, Flyway
- **API**: RESTful CRUD for Message management
- **Docs**: [backend/CLAUDE.md](backend/CLAUDE.md)

See [backend/README.md](backend/README.md) for setup instructions.

### Frontend

Next.js-based message management application with full CRUD functionality.

- **Tech Stack**: Next.js 15+, TypeScript, shadcn/ui, TanStack Query, Orval
- **API Client**: Auto-generated from OpenAPI spec
- **Docs**: [frontend/CLAUDE.md](frontend/CLAUDE.md)

See [frontend/README.md](frontend/README.md) for setup instructions.

## Documentation

- [GITIGNORE_RULES.md](docs/GITIGNORE_RULES.md) - `.gitignore` management rules and guidelines

## Quick Start with Docker

### Development Mode (with hot reload)

```bash
# Start all services (frontend, backend, postgres, nginx)
docker-compose up

# Access the application
# - Application: http://localhost:3000
# - Backend API: http://localhost:8080/api
# - Database: localhost:5432
```

Frontend connects directly to backend at `http://localhost:8080` for development.

### Production Mode (optimized build)

```bash
# Start production services with nginx reverse proxy
docker-compose -f docker-compose.yml up

# Access the application
# - Application: http://localhost (port 80)
# - All traffic routed through nginx:
#   - / → frontend (internal port 3000)
#   - /api → backend (internal port 8080)
```

Frontend connects to backend via `/api` (relative URL through nginx).

### Architecture Explained

**Development vs Production**:

| Mode | Access Point | Frontend API URL | Reason |
|------|--------------|------------------|---------|
| Development | http://localhost:3000 | `http://localhost:8080` | Direct backend access for debugging |
| Production | http://localhost | `/api` | Nginx reverse proxy for production-ready setup |

**Why Nginx Reverse Proxy in Production?**

The frontend's `NEXT_PUBLIC_API_URL` is bundled into browser JavaScript. Using Docker internal hostnames like `http://backend:8080` fails because external browsers cannot resolve them. Nginx solves this by:

1. Providing a single access point (http://localhost)
2. Both frontend and backend accessible through same domain (no CORS)
3. Relative URLs work (`/api` resolves correctly)
4. Production-ready and scalable
5. Easy to add SSL termination later

See [CLAUDE.md](CLAUDE.md#docker-deployment) for more details.

## Development

This project is configured for IntelliJ IDEA with Java 21 and Node.js 20+.

## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master

## License

This is a sandbox project for experimentation purposes.