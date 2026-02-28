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

**対象**: バックエンド・フロントエンド・ブラウザの3層が同時に関与するクリティカルフローのみ

#### E2E テスト追加判断フロー

```
新機能のテストを書く前に、以下の質問に答えてください:

Q1: バックエンド API とフロントエンドの両方が同時に関与するか？
 └ No → Unit/Component テストで対応

Q2: Component テストや Backend 統合テストで代替できないか？
 └ Yes (代替できる) → 代替テストを書く（E2E ではない）

Q3: 既存の E2E テストと実質同一のフローか？
 └ Yes → 既存テストに吸収する

Q4: 追加後も上限（30件）に収まるか？
 └ No → 他の E2E を削減してから追加する

全て通過した場合のみ E2E テストを追加する
```

#### E2E にしてはいけないケース（禁止パターン）

| 検証内容                                         | E2E にしてはいけない理由        | 代替テスト種別                          |
| ------------------------------------------------ | ------------------------------- | --------------------------------------- |
| フォームバリデーション（空欄・文字数・パターン） | Zod スキーマで完結する          | Unit テスト                             |
| ボタンの表示/非表示（`usePermission` フック）    | フック単体で検証可能            | Unit テスト（`renderHook`）             |
| レスポンシブレイアウト（カード↔テーブル切替）    | CSS 検証はブラウザ不要          | Component テスト（`matchMedia` モック） |
| i18n テキスト表示（日本語/英語の切替）           | LocaleContext モックで検証可能  | Component テスト                        |
| localStorage の読み書き                          | jsdom で十分                    | Unit テスト                             |
| debounce の動作                                  | `vi.useFakeTimers()` で制御可能 | Unit テスト                             |
| HTTP エラーレスポンス形式（403/401）             | Backend 責務・Playwright 不要   | Backend MockMvc 統合テスト              |
| 既存 E2E テストと実質同一フロー                  | 重複・メンテコスト増            | 既存テストで代替                        |

#### E2E テストの例

```typescript
// ✅ 良い例: 認証フロー（3層関与・代替不可）
test('ログインに成功してメッセージ一覧を表示する', async ({ page }) => {
  await page.goto('/login');
  await page.getByTestId('login-username-input').fill('admin');
  await page.getByTestId('login-password-input').fill('admin123');
  await page.getByTestId('login-submit-button').click();
  await expect(page).toHaveURL('/');
  await expect(page.getByTestId('search-input')).toBeVisible();
});

// ❌ 悪い例: フォームバリデーション（→ Unit テストへ移行）
// test('空欄のまま送信するとエラーが表示される', ...)

// ❌ 悪い例: 権限ボタン表示（→ Unit テストへ移行）
// test('VIEWER は作成ボタンを表示しない', ...)
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

**上限**: 30テスト以下（CI 5分以内）

**カバレッジ目標**: クリティカルフロー（認証・CRUD・権限）の 100%（テスト数は最小限）

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

E2E 追加前に、以下のチェックリストで判断する:

- [ ] バックエンドとフロントエンドの両方が同時に関与するフローか？
- [ ] Component テストや Backend 統合テストで代替できないか？
- [ ] 既存 E2E テストと実質同一のフローではないか？
- [ ] 追加後も上限（30件）に収まるか？

上記を全て満たす場合のみ追加:

- [ ] 認証が必要なフローのログインテスト
- [ ] 主要な CRUD フロー（作成・編集・削除）
- [ ] ADMIN/VIEWER 両ロールの操作フロー

## 9. 関連ドキュメント

- [全体テスト戦略](../../docs/quality/TEST_STRATEGY.md) - システム全体の方針
- [Backend テスト戦略](../../backend/docs/TEST_STRATEGY.md) - JUnit5・Testcontainers
