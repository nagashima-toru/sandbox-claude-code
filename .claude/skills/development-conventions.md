# Development Conventions

## Code Style

### Java (Backend)
- Follow Clean Architecture (domain → application → infrastructure → presentation)
- Write JUnit tests for all new classes
- Use Testcontainers for integration tests

### TypeScript (Frontend)
- Functional components with TypeScript
- Named exports over default exports
- Co-locate tests and stories with components

## File Naming

| Type | Convention | Example |
|------|------------|---------|
| React Component | PascalCase | `MessageTable.tsx` |
| Hook | camelCase + use | `useMessages.ts` |
| Story | PascalCase + .stories | `MessageTable.stories.tsx` |
| Test | PascalCase + .test | `MessageTable.test.tsx` |

## Git Conventions

- Branch: `feature/`, `bugfix/`, `hotfix/`
- Commits: Descriptive, atomic changes
- PR: Run `./scripts/ci-check-local.sh` before creating

## Testing

### Backend
- Coverage: ≥80% lines, ≥75% branches
- Use `./mvnw verify` (not `integration-test`)

### Frontend
- Coverage: ≥80% statements/functions/lines, ≥70% branches
- Vitest for unit/component tests
- Playwright for E2E

## API Changes Workflow

1. Update OpenAPI spec
2. `cd backend && ./mvnw verify`
3. `cd frontend && pnpm generate:api`
4. Update components as needed
