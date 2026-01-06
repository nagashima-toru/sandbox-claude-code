# Sandbox Claude Code

[![Backend CI](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/backend-ci.yml)
[![CodeQL](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/codeql.yml/badge.svg)](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/codeql.yml)

A sandbox repository for experimenting with Claude Code.

## Project Structure

```
sandbox-claude-code/
├── backend/          # Spring Boot API application (Java 23, Maven)
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

- **Tech Stack**: Java 23, Spring Boot 3.4.1, PostgreSQL, MyBatis, Flyway
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

## Development

This project is configured for IntelliJ IDEA with Java 23 and Node.js 20+.

## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master

## License

This is a sandbox project for experimentation purposes.