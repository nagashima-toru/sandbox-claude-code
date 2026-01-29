# Frontend Testing

## Commands

```bash
cd frontend
pnpm test           # Run Vitest
pnpm test:e2e       # Run Playwright
pnpm test:coverage  # With coverage
```

## Coverage Thresholds

- Lines: 80%
- Functions: 80%
- Branches: 70%
- Statements: 80%

## Test Structure

```typescript
// ComponentName.test.tsx
import { render, screen } from '@testing-library/react';
import { ComponentName } from './ComponentName';

describe('ComponentName', () => {
  it('renders correctly', () => {
    render(<ComponentName />);
    expect(screen.getByText('...')).toBeInTheDocument();
  });
});
```

## Exclusions (vitest.config.ts)

- Auto-generated: `src/lib/api/generated/**`
- Framework entry: `src/app/layout.tsx`, `src/app/page.tsx`
- Configuration: `src/lib/api/client.ts`

## Testing Levels

| Level | Tool | Scope |
|-------|------|-------|
| Unit | Vitest | Utils, hooks |
| Component | Vitest + RTL | Components |
| E2E | Playwright | Full flows |

See frontend/CLAUDE.md for detailed testing strategy.
