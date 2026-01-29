# Orval API Code Generation

Generate TypeScript API client from OpenAPI specification.

## Command

```bash
cd frontend && pnpm generate:api
```

## When to Run

Run after backend API changes:

```bash
# 1. Backend: Generate OpenAPI spec
cd backend && ./mvnw verify

# 2. Frontend: Generate API client
cd frontend && pnpm generate:api
```

## Generated Files

- `src/lib/api/generated/messages.ts` - Types, API functions, React Query hooks
- DO NOT edit generated files manually

## Usage

```typescript
import { useGetAllMessages, useCreateMessage } from '@/lib/api/generated/messages';

// In component
const { data, isLoading } = useGetAllMessages();
const mutation = useCreateMessage();
```

See [docs/ORVAL_API_GENERATION.md](../docs/ORVAL_API_GENERATION.md) for detailed documentation.
