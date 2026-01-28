# CLAUDE.md

This file provides guidance to Claude Code when working with the frontend application.

## Project Overview

Next.js-based message management application with full CRUD functionality.

## Tech Stack

- **Framework**: Next.js 15+ (App Router)
- **Language**: TypeScript
- **UI Library**: shadcn/ui
- **Styling**: Tailwind CSS
- **Package Manager**: pnpm
- **API Code Generation**: Orval (OpenAPI → TypeScript + React Query hooks)
- **HTTP Client**: axios
- **State Management**: TanStack Query (React Query)
- **Form Management**: React Hook Form + Zod
- **Testing**: Vitest + Testing Library + Playwright (E2E)
- **Component Development**: Storybook 10+ with MSW integration
- **Dev Server Port**: 3000
- **Storybook Port**: 6006

## Build Commands

```bash
# Install dependencies
pnpm install

# Generate API client from OpenAPI spec
pnpm generate:api

# Development
pnpm dev

# Build
pnpm build

# Production
pnpm start

# Test
pnpm test

# E2E test
pnpm test:e2e

# Lint & Type check
pnpm lint
pnpm type-check

# Storybook
pnpm storybook           # port 6006
pnpm build-storybook
```

## Project Structure

```
frontend/
├── src/
│   ├── app/                    # Next.js App Router
│   ├── components/             # React components
│   │   ├── ui/                 # shadcn/ui components
│   │   ├── messages/           # Message-specific components
│   │   └── common/             # Common components
│   ├── lib/
│   │   ├── api/
│   │   │   ├── generated/      # Orval generated (DO NOT EDIT)
│   │   │   └── client.ts       # Axios instance
│   │   ├── validations/        # Zod schemas
│   │   └── utils.ts
│   ├── hooks/                  # Custom React hooks
│   └── types/                  # Manual type definitions
├── tests/
│   ├── unit/
│   ├── component/
│   └── e2e/
├── orval.config.ts
└── vitest.config.ts
```

**Note**: `src/lib/api/generated/` is auto-generated. DO NOT manually edit.

## フロントエンドの開発プロセス（実装〜単体テスト）

1. 画面レイアウト設計
2. コンポーネント分離設計
3. コンポーネント実装 & 単体テスト
4. Storybook 作成（コンポーネント）
5. review（コンポーネント）
6. 画面レイアウト作成
7. Storybook 作成（画面レイアウト）
8. review（画面レイアウト）
9. API 呼び出し実装（OpenAPIからClient自動生成も含む）
10. Storybook 修正
11. テスト作成実施（Playwright）
12. review（テスト結果）

## Features

### Message List Page (/)

- Responsive table with ID, Code, Content, Actions columns
- Search/filter (debounced)
- Sort by column headers
- Pagination (10, 25, 50, 100 items)
- CRUD operations via modals

### Validation (React Hook Form + Zod)

```typescript
// src/lib/validations/message.ts
export const messageSchema = z.object({
  code: z.string().min(1).max(50),
  content: z.string().min(1).max(500),
});
```

## API Code Generation Quick Reference

```bash
# After backend API changes
cd backend && ./mvnw verify    # Generate OpenAPI
cd frontend && pnpm generate:api  # Generate client
```

**Usage**:

```typescript
import { useGetAllMessages, useCreateMessage } from '@/lib/api/generated/messages';

const { data, isLoading } = useGetAllMessages();
const mutation = useCreateMessage();
```

See [docs/ORVAL_API_GENERATION.md](../docs/ORVAL_API_GENERATION.md) for details.

## Storybook Quick Reference

```bash
pnpm storybook  # Start at http://localhost:6006
```

**Story template**:

```typescript
import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import { MyComponent } from './MyComponent';

const meta = {
  title: 'Category/MyComponent',
  component: MyComponent,
  tags: ['autodocs'],
} satisfies Meta<typeof MyComponent>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    /* props */
  },
};
```

See [docs/STORYBOOK.md](../docs/STORYBOOK.md) for details.

## Error Handling

### Client-side

- Validation errors under form fields
- Network errors in modal/form
- Not found messages

### Server-side

| Status | Display                       |
| ------ | ----------------------------- |
| 400    | Validation errors from server |
| 404    | "Message not found"           |
| 409    | "Code already exists"         |
| 500    | "An error occurred"           |

## Responsive Design

**Breakpoints (Tailwind)**:

- sm: 640px, md: 768px, lg: 1024px, xl: 1280px

**Mobile**:

- Table → Card layout (< 768px)
- Modal → Full screen
- Touch-friendly (min 44x44px)

## Testing Strategy

### Test Types

| Type      | Tool         | Scope                    |
| --------- | ------------ | ------------------------ |
| Unit      | Vitest       | Utils, validation, hooks |
| Component | Vitest + RTL | Components               |
| E2E       | Playwright   | Full flows               |

### Coverage Thresholds

- Lines: 80%
- Functions: 80%
- Branches: 70%
- Statements: 80%

### Exclusions

- Auto-generated: `src/lib/api/generated/**`
- Framework entry: `src/app/layout.tsx`, `src/app/page.tsx`
- Configuration: `src/lib/api/client.ts`

## Code Style Guidelines

- **Components**: Functional with TypeScript
- **Props**: Explicit interface/type definitions
- **Exports**: Named over default
- **Hooks**: Extract reusable logic, prefix with `use`

**File naming**:
| Type | Convention | Example |
|------|------------|---------|
| Component | PascalCase | `MessageTable.tsx` |
| Hook | camelCase + use | `useMessages.ts` |
| Story | + .stories | `MessageTable.stories.tsx` |
| Test | + .test | `MessageTable.test.tsx` |

## Accessibility (a11y)

- Semantic HTML
- ARIA labels for interactive elements
- Keyboard navigation (Tab, Enter, Escape)
- Focus management in modals
- WCAG AA color contrast

## Documentation Index

| Document                                          | Description                             |
| ------------------------------------------------- | --------------------------------------- |
| [Storybook](../docs/STORYBOOK.md)                 | Full Storybook guide, MSW, a11y testing |
| [Orval API](../docs/ORVAL_API_GENERATION.md)      | API client generation details           |
| [Docker Deployment](../docs/DOCKER_DEPLOYMENT.md) | Docker development modes                |

## Notes

- This is a sandbox project for experimenting with Claude Code
- Backend API follows Clean Architecture (see backend/CLAUDE.md)
- Use latest stable versions of dependencies
- Follow Next.js App Router best practices
- Maintain type safety throughout
