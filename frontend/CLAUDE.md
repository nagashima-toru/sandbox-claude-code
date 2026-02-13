# CLAUDE.md

This file provides guidance to Claude Code when working with the frontend application.

## Project Overview

Next.js-based message management application with full CRUD functionality.

## Tech Stack

- **Framework**: Next.js 16+ (App Router)
- **Language**: TypeScript
- **UI Library**: shadcn/ui
- **Styling**: Tailwind CSS v4
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

## Tailwind CSS v4 Migration

This project uses **Tailwind CSS v4**, which introduced significant changes from v3:

### Key Changes

1. **PostCSS Plugin Separation**
   - The PostCSS plugin is now in a separate package: `@tailwindcss/postcss`
   - Updated `postcss.config.js` to use `@tailwindcss/postcss` instead of `tailwindcss`

2. **New CSS Import Syntax**
   - Changed from `@tailwind` directives to `@import "tailwindcss"`
   - `src/app/globals.css` now uses the new v4 syntax

3. **Dark Mode Configuration**
   - `darkMode` in `tailwind.config.ts` changed from array `['class']` to string `'class'`

4. **Removed @apply Usage**
   - Replaced `@apply` directives with direct CSS properties
   - Example: `@apply border-border` → `border-color: hsl(var(--border))`

### Configuration Files

**postcss.config.js**:

```javascript
module.exports = {
  plugins: {
    '@tailwindcss/postcss': {},
    autoprefixer: {},
  },
};
```

**tailwind.config.ts**:

```typescript
const config: Config = {
  darkMode: 'class', // Changed from ['class']
  // ... rest of config
};
```

**src/app/globals.css**:

```css
@import 'tailwindcss';

:root {
  /* CSS variables */
}

* {
  border-color: hsl(var(--border));
}

body {
  background-color: hsl(var(--background));
  color: hsl(var(--foreground));
}
```

### Migration Notes

- The `tailwind.config.ts` file is still supported and used for theme customization
- CSS variables (e.g., `--background`, `--foreground`) continue to work as before
- shadcn/ui components are fully compatible with v4
- No changes required in component files

### Resources

- [Tailwind CSS v4 Documentation](https://tailwindcss.com/docs)
- [v4 Migration Guide](https://tailwindcss.com/docs/upgrade-guide)

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

### Permission Control

This application implements role-based permission control for UI elements:

**Supported Roles**:

- `ADMIN`: Full access (create, edit, delete)
- `VIEWER`: Read-only access

**Implementation**:

```typescript
// Get current user information
import { useAuth } from '@/contexts/AuthContext';

function MyComponent() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return <Loading />;
  }

  // user.role is 'ADMIN' or 'VIEWER'
  const canEdit = user?.role === 'ADMIN';

  return (
    <>
      {canEdit && <Button>Edit</Button>}
    </>
  );
}
```

**Permission Hooks**:

```typescript
import { usePermission } from '@/hooks/usePermission';

function MyComponent() {
  const { canCreate, canUpdate, canDelete, isReadOnly } = usePermission();

  return (
    <>
      {canCreate && <Button>Create</Button>}
      {canUpdate && <Button>Update</Button>}
      {canDelete && <Button>Delete</Button>}
      {isReadOnly && <InfoMessage>閲覧のみ可能です</InfoMessage>}
    </>
  );
}
```

**Role-Based Component**:

```typescript
import { RoleBasedComponent } from '@/components/common/RoleBasedComponent';

function MyComponent() {
  return (
    <>
      <RoleBasedComponent allowedRoles={['ADMIN']}>
        <Button>Admin Only</Button>
      </RoleBasedComponent>

      <RoleBasedComponent allowedRoles={['ADMIN', 'VIEWER']}>
        <p>Visible to all</p>
      </RoleBasedComponent>
    </>
  );
}
```

**Readonly Form Mode**:

Forms automatically switch to readonly mode for VIEWER role:

- All input fields are disabled
- Submit and delete buttons are hidden
- Only "Close" button is shown

**Testing Permission UI**:

Use test credentials for different roles:

- ADMIN: `testuser` / `password123`
- VIEWER: `viewer` / `password123`

**Security Note**: Permission checks in the UI are for display control only. All security enforcement is handled by the backend API.

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

See [docs/frontend/ORVAL_API_GENERATION.md](../docs/frontend/ORVAL_API_GENERATION.md) for details.

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

See [docs/frontend/STORYBOOK.md](../docs/frontend/STORYBOOK.md) for details.

## React Context Pattern

### Creating a Context

Always include `| undefined` in the Context type to detect usage outside of the Provider at compile time. Create a custom Hook that checks for `undefined` and throws an error.

**Pattern**:

```typescript
import { createContext, useContext } from 'react';

export interface MyContextValue {
  user: User | null;
  isLoading: boolean;
  setUser: (user: User | null) => void;
}

export const MyContext = createContext<MyContextValue | undefined>(undefined);

export function useMyContext(): MyContextValue {
  const context = useContext(MyContext);

  if (context === undefined) {
    throw new Error('useMyContext must be used within MyProvider');
  }

  return context;
}

export function MyProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const value: MyContextValue = {
    user,
    isLoading,
    setUser,
  };

  return <MyContext.Provider value={value}>{children}</MyContext.Provider>;
}
```

### Using Context in Storybook

When creating Stories that use Context, provide both `args` and `render` properties. For custom Provider values, wrap the component in `render`.

**Pattern**:

```typescript
import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import { MyContext, MyProvider } from './MyContext';
import { MyComponent } from './MyComponent';

const meta = {
  title: 'Components/MyComponent',
  component: MyProvider,
  tags: ['autodocs'],
} satisfies Meta<typeof MyProvider>;

export default meta;
type Story = StoryObj<typeof meta>;

// Using default Provider
export const Default: Story = {
  args: {
    children: <MyComponent />,
  },
};

// Custom Provider value
export const CustomState: Story = {
  args: {
    children: <MyComponent />,
  },
  render: (_args) => (
    <MyContext.Provider
      value={{
        user: { id: 1, username: 'admin', role: 'ADMIN' },
        isLoading: false,
        setUser: () => {},
      }}
    >
      <MyComponent />
    </MyContext.Provider>
  ),
};
```

### Testing Context Hooks

Use static `import` instead of dynamic `require()` for Context in tests. Create custom test wrappers with `AuthContext.Provider` to test different states.

**Pattern**:

```typescript
import { renderHook } from '@testing-library/react';
import { MyContext, type MyContextValue } from './MyContext';
import { useMyHook } from './useMyHook';

it('should work with custom context value', () => {
  const customValue: MyContextValue = {
    user: { id: 1, username: 'test' },
    isLoading: false,
    setUser: () => {},
  };

  const wrapper = ({ children }: { children: ReactNode }) => (
    <MyContext.Provider value={customValue}>
      {children}
    </MyContext.Provider>
  );

  const { result } = renderHook(() => useMyHook(), { wrapper });

  expect(result.current).toBeDefined();
});
```

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

### Test Best Practices

- **実装と同時にテストを修正**: 既存コードに影響を与える変更（Context の型変更、新しい Hook の追加など）を行う際は、実装と同時に影響を受けるテストも修正する
- **Next.js Hooks のモック**: `useRouter`, `useSearchParams` などの Next.js Hooks を使用するコンポーネントをテストする場合は、`next/navigation` のモックを準備する

  ```typescript
  // Test file
  vi.mock('next/navigation', () => ({
    useRouter: () => ({
      push: vi.fn(),
      replace: vi.fn(),
      prefetch: vi.fn(),
    }),
  }));
  ```

- **QueryClientProvider のラップ**: React Query を使用する Hook をテストする場合は、テスト用の wrapper を作成する

  ```typescript
  function createWrapper() {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });
    return ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
  }
  ```

## Working Directory Management

**重要**: 常にプロジェクトルート (`/Users/.../sandbox-claude-code`) で作業を開始する

### ルール

1. **基本は常にルートディレクトリ**: git コマンド、スクリプト実行は基本的にルートから実行
2. **frontend での作業時**:

   ```bash
   # ❌ 悪い例
   cd frontend
   pnpm install
   git add src/...  # パスが間違う

   # ✅ 良い例
   cd frontend && pnpm install && cd ..
   git add frontend/src/...
   ```

3. **pwd で現在位置を常に確認**: コマンド実行前に `pwd` で位置を確認する習慣をつける
4. **作業完了後は必ずルートに戻る**: `cd ..` でルートディレクトリに戻る

### よくある問題

- `git add` や `git commit` でファイルが見つからない → 現在のディレクトリを確認
- パスの指定が相対パスか絶対パスか不明 → `pwd` で確認してから実行

## PR Creation

**IMPORTANT**: PR 作成時は必ず適切なテンプレートを使用すること。テンプレートを使わないと **Implementation Check が失敗します**。

### Story PR の場合

```bash
gh pr create --base feature/issue-[N]-[epic-name] \
             --head feature/issue-[N]-[epic-name]-story[M] \
             --template .github/PULL_REQUEST_TEMPLATE/story.md
```

**例**: Epic #133 の Story 5

```bash
gh pr create --base feature/issue-133-permission-ui \
             --head feature/issue-133-permission-ui-story5 \
             --template .github/PULL_REQUEST_TEMPLATE/story.md
```

### Epic PR の場合

```bash
gh pr create --base master \
             --head feature/issue-[N]-[epic-name] \
             --template .github/PULL_REQUEST_TEMPLATE/epic.md
```

### Spec PR の場合

```bash
gh pr create --base master \
             --head feature/issue-[N]-spec \
             --template .github/PULL_REQUEST_TEMPLATE/spec.md
```

### 自動化スクリプト

プロジェクトルートから以下のスクリプトを使用すると、テンプレートを自動選択できます:

```bash
./scripts/create-story-pr.sh [issue-number] [story-number]
```

## React Query Troubleshooting

### 症状

- ログイン後、古いユーザー情報が表示される
- ロールを切り替えても権限が反映されない
- API を呼び出したはずなのに古いデータが表示される

### 原因

React Query のキャッシュ設定（`staleTime`, `gcTime`）により、古いデータがキャッシュに残っている可能性があります。

AuthContext の設定例:

```typescript
useGetCurrentUser({
  query: {
    staleTime: 5 * 60 * 1000, // 5分間キャッシュ
    gcTime: 10 * 60 * 1000, // 10分間メモリ保持
    // ...
  },
});
```

### 解決方法

1. **ハードリフレッシュ**: `Cmd+Shift+R` (Mac) / `Ctrl+Shift+R` (Windows/Linux)
   - ブラウザキャッシュと React Query キャッシュを両方クリア

2. **ログアウト→ログイン**: 新しいトークンで再認証
   - localStorage のトークンがクリアされ、新しいユーザー情報を取得

3. **React Query DevTools で確認**: キャッシュの状態を可視化

   ```typescript
   import { ReactQueryDevtools } from '@tanstack/react-query-devtools';

   // layout.tsx に追加
   <ReactQueryDevtools initialIsOpen={false} />
   ```

4. **開発時のキャッシュ無効化** (必要に応じて):

   ```typescript
   useGetCurrentUser({
     query: {
       staleTime: 0, // 常に最新データを取得
       gcTime: 0, // キャッシュしない
     },
   });
   ```

### 予防策

- 認証状態が変わる操作（ログイン、ロール変更）の後は、ページリフレッシュを促す UI を表示
- `refetch()` を明示的に呼び出して最新データを取得
- 重要な権限チェックはバックエンドで実施（フロントエンドは表示制御のみ）

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

| Document                                                      | Description                             |
| ------------------------------------------------------------- | --------------------------------------- |
| [Storybook](../docs/frontend/STORYBOOK.md)                    | Full Storybook guide, MSW, a11y testing |
| [Orval API](../docs/frontend/ORVAL_API_GENERATION.md)         | API client generation details           |
| [Docker Deployment](../docs/environment/DOCKER_DEPLOYMENT.md) | Docker development modes                |

## Notes

- This is a sandbox project for experimenting with Claude Code
- Backend API follows Clean Architecture (see backend/CLAUDE.md)
- Use latest stable versions of dependencies
- Follow Next.js App Router best practices
- Maintain type safety throughout
