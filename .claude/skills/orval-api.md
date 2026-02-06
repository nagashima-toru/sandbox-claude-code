# Orval API Generation

## Quick Command

**Recommended**:
```bash
./scripts/generate-api.sh
```

**Direct command** (alternative):
```bash
cd frontend && pnpm generate:api
```

## Workflow

```
Backend API change
      ↓
cd backend && ./mvnw verify
      ↓
cd frontend && pnpm generate:api
      ↓
TypeScript catches type mismatches
```

## Generated Code

Location: `src/lib/api/generated/messages.ts`

Contains:
- Type definitions (from OpenAPI)
- API functions (axios-based)
- React Query hooks

## Usage

```typescript
import {
  useGetAllMessages,
  useCreateMessage,
  MessageRequest,
} from '@/lib/api/generated/messages';

// Query
const { data, isLoading } = useGetAllMessages();

// Mutation
const mutation = useCreateMessage({
  onSuccess: () => queryClient.invalidateQueries(['getAllMessages']),
});
mutation.mutate({ data: { code: '...', content: '...' } });
```

## Rules

- **DO NOT** edit files in `src/lib/api/generated/`
- Always use generated hooks (not raw axios)
- Re-run after backend changes

See [docs/ORVAL_API_GENERATION.md](../../docs/ORVAL_API_GENERATION.md)
