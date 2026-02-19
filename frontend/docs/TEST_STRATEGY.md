# Frontend テスト戦略

## 1. テスト戦略の目的

- **品質保証**: コンポーネントの正確な挙動を保証
- **リグレッション防止**: UI 変更による既存機能の破壊を検出
- **ドキュメント**: テストコードが仕様書の役割を果たす
- **開発速度**: 安心してリファクタリング・機能追加できる環境

## 2. テストピラミッド

```
        /\
       /  \  E2E Tests (5%)
      /    \  Playwright
     /______\
    /        \  Component Tests (25%)
   / Vitest + \  React Testing Library
  /   RTL      \
 /______________\
/                \
/  Unit Tests     \ (70%)
/ Vitest            \  Utils, Hooks, Validations
/____________________\
```

### 推奨比率

- **Unit Tests**: 70% - Utils・Hooks・バリデーション（高速・独立）
- **Component Tests**: 25% - コンポーネントのレンダリング・インタラクション
- **E2E Tests**: 5% - クリティカルなユーザーフロー

## 3. テストタイプ別戦略

### 3.1 Unit Tests（Vitest）

**対象**: ユーティリティ関数・カスタム Hooks・バリデーション

```typescript
// src/lib/validations/message.test.ts
import { messageSchema } from './message';

describe('messageSchema', () => {
  it('有効なデータを受け入れる', () => {
    const result = messageSchema.safeParse({ code: 'MSG001', content: 'Hello' });
    expect(result.success).toBe(true);
  });

  it('空の code をリジェクトする', () => {
    const result = messageSchema.safeParse({ code: '', content: 'Hello' });
    expect(result.success).toBe(false);
  });
});
```

**カバレッジ目標**: 90%+

---

### 3.2 Component Tests（Vitest + React Testing Library）

**対象**: React コンポーネント単体のレンダリング・ユーザーインタラクション

```typescript
// src/components/messages/MessageTable.test.tsx
import { render, screen } from '@testing-library/react';
import { MessageTable } from './MessageTable';

const createWrapper = () => {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('MessageTable', () => {
  it('メッセージ一覧を表示する', () => {
    render(<MessageTable messages={[{ id: 1, code: 'MSG001', content: 'Hello' }]} />, {
      wrapper: createWrapper(),
    });
    expect(screen.getByText('MSG001')).toBeInTheDocument();
  });

  it('空のメッセージ一覧でメッセージを表示する', () => {
    render(<MessageTable messages={[]} />, { wrapper: createWrapper() });
    expect(screen.getByText('データがありません')).toBeInTheDocument();
  });
});
```

**カバレッジ目標**: 80%+

---

### 3.3 Context・Hooks テスト

**パターン**: 静的 `import` を使用し、テスト用 wrapper で Context を提供する

```typescript
import { renderHook } from '@testing-library/react';
import { AuthContext, type AuthContextValue } from '@/contexts/AuthContext';
import { usePermission } from '@/hooks/usePermission';

it('ADMIN ロールで canCreate が true になる', () => {
  const contextValue: AuthContextValue = {
    user: { id: 1, username: 'admin', role: 'ADMIN' },
    isLoading: false,
    setUser: vi.fn(),
  };

  const wrapper = ({ children }: { children: ReactNode }) => (
    <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
  );

  const { result } = renderHook(() => usePermission(), { wrapper });
  expect(result.current.canCreate).toBe(true);
});
```

---

### 3.4 Next.js Hooks のモック

`useRouter`・`useSearchParams` などを使うコンポーネントのテストでは `next/navigation` をモックする。

```typescript
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn(), prefetch: vi.fn() }),
  useSearchParams: () => new URLSearchParams(),
  usePathname: () => '/',
}));
```

---

### 3.5 E2E Tests（Playwright）

**対象**: 認証フロー・権限制御・主要 CRUD フローのブラウザ操作

```typescript
// tests/e2e/auth.spec.ts
test('ログインに成功してメッセージ一覧を表示する', async ({ page }) => {
  await page.goto('/login');
  await page.getByTestId('login-username-input').fill('admin');
  await page.getByTestId('login-password-input').fill('admin123');
  await page.getByTestId('login-submit-button').click();
  await expect(page).toHaveURL('/');
  await expect(page.getByTestId('search-input')).toBeVisible();
});

test('VIEWER は作成ボタンを表示しない', async ({ page }) => {
  await login(page, 'viewer', 'viewer123');
  await expect(page.getByTestId('create-button')).not.toBeVisible();
});
```

**実行**:

```bash
# ローカル実行
./scripts/e2e-test-local.sh

# 手動実行
docker compose up postgres -d
cd backend && ./mvnw spring-boot:run &
cd frontend && pnpm test:e2e
```

**カバレッジ目標**: クリティカルフローの 100%（テスト数は最小限）

## 4. テストツールとフレームワーク

| ツール                    | 用途                            | 対象                     |
| ------------------------- | ------------------------------- | ------------------------ |
| **Vitest**                | テストランナー・Unit/Component  | Utils, Hooks, Components |
| **React Testing Library** | コンポーネントテスト            | Components               |
| **Playwright**            | E2E テスト                      | Full flows               |
| **MSW**                   | API モック（Storybook・テスト） | Component/E2E            |
| **Zod**                   | バリデーション（テスト対象）    | バリデーション層         |

## 5. テスト命名規則

### ファイル命名

```
ComponentName.tsx → ComponentName.test.tsx
useHookName.ts   → useHookName.test.ts
utils.ts         → utils.test.ts
```

### テストの記述スタイル

```typescript
describe('ComponentName または機能名', () => {
  it('期待する状態・入力 + 期待する挙動を日本語で記述', () => {
    // Arrange
    // Act
    // Assert
  });
});
```

## 6. カバレッジ目標

| 種別         | Lines | Functions | Branches | Statements |
| ------------ | ----- | --------- | -------- | ---------- |
| 全体         | 80%+  | 80%+      | 70%+     | 80%+       |
| Utils・Hooks | 90%+  | 90%+      | 80%+     | 90%+       |
| Components   | 80%+  | 80%+      | 70%+     | 80%+       |

### カバレッジ確認

```bash
# カバレッジレポート生成
cd frontend && pnpm test:coverage

# 除外対象
# - src/lib/api/generated/**  （自動生成）
# - src/app/layout.tsx        （フレームワークエントリ）
# - src/lib/api/client.ts     （設定ファイル）
```

## 7. テストのベストプラクティス

### DO（推奨）

✅ **実装と同時にテストを修正**: Context 型変更・Hook 追加時は影響テストも同時修正

✅ **QueryClientProvider を wrapper で提供**: React Query を使う Hook のテストには必須

```typescript
function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}
```

✅ **MSW でモック**: コンポーネントテストで API を直接呼ばない

✅ **data-testid を使う**: E2E テストのセレクターは `data-testid` で安定させる

### DON'T（非推奨）

❌ **実装詳細をテスト**: `useState` の内部状態を直接検証しない。公開 API 経由で確認

❌ **テスト間の依存**: 各テストは独立して実行可能にする（`beforeEach` でリセット）

❌ **手動 axios 呼び出し**: 生成された React Query hooks を使い、テストでは MSW でモック

## 8. 実装チェックリスト

新機能追加時のテスト実装ガイド:

### コンポーネント

- [ ] 正常系レンダリングテスト
- [ ] 空/ローディング/エラー状態のテスト
- [ ] ユーザーインタラクションテスト（クリック、入力）
- [ ] 権限によって表示が変わる場合はロール別テスト

### カスタム Hook

- [ ] 正常系テスト
- [ ] 異常系・エッジケーステスト
- [ ] Context を使う場合は wrapper でラップ

### バリデーション（Zod）

- [ ] 有効なデータのテスト
- [ ] 各フィールドの無効値テスト
- [ ] 境界値テスト

### E2E

- [ ] 認証が必要なフローのログインテスト
- [ ] ADMIN/VIEWER 両ロールでの動作確認
- [ ] 主要な CRUD フロー

## 9. 関連ドキュメント

- [全体テスト戦略](../../docs/quality/TEST_STRATEGY.md) - システム全体の方針
- [Backend テスト戦略](../../backend/docs/TEST_STRATEGY.md) - JUnit5・Testcontainers
