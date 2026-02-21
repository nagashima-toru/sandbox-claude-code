# Frontend ベストプラクティス

このドキュメントは `/review-implementation` スキルおよび `/implement-epic` スキルが参照する
フロントエンド実装の規約・パターン集です。

---

## 1. コンポーネント設計

### named export のみ

```typescript
// ✅ 正しい（named export）
export const MessageTable = ({ messages }: MessageTableProps) => { ... };

// ❌ 禁止（default export）
export default function MessageTable() { ... }
```

### Props は explicit interface/type 定義

```typescript
// ✅ 正しい
interface MessageTableProps {
  messages: Message[];
  onEdit: (id: number) => void;
}

export const MessageTable = ({ messages, onEdit }: MessageTableProps) => { ... };

// ❌ 避ける（型が不明確）
export const MessageTable = (props: any) => { ... };
```

### PascalCase ファイル名

| ファイル種別   | 命名規則                         | 例                         |
| -------------- | -------------------------------- | -------------------------- |
| コンポーネント | PascalCase                       | `MessageTable.tsx`         |
| Hook           | camelCase + `use` プレフィックス | `useMessages.ts`           |
| Storybook      | + `.stories`                     | `MessageTable.stories.tsx` |
| テスト         | + `.test`                        | `MessageTable.test.tsx`    |
| Context        | PascalCase + `Context`           | `AuthContext.tsx`          |

---

## 2. Hook パターン

### `use` プレフィックス必須

```typescript
// ✅ 正しい
export function useMessages() { ... }
export function usePermission() { ... }

// ❌ 禁止
export function getMessages() { ... }
```

### React Query Hook のテスト: QueryClientProvider ラップ必須

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

it('should fetch messages', () => {
  const { result } = renderHook(() => useGetAllMessages(), {
    wrapper: createWrapper(),
  });
  // ...
});
```

### Orval 生成 Hook を使用（手動 axios 呼び出し禁止）

```typescript
// ✅ 正しい（Orval 生成 Hook を使用）
import { useGetAllMessages, useCreateMessage } from '@/lib/api/generated/messages';

const { data, isLoading } = useGetAllMessages();
const mutation = useCreateMessage();

// ❌ 禁止（手動 axios 呼び出し）
import axios from 'axios';
const response = await axios.get('/api/messages');
```

---

## 3. Context パターン

### 型定義に `| undefined` を含める

```typescript
// ✅ 正しい（| undefined を含む）
export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

// ❌ 禁止（undefined チェックが働かない）
export const AuthContext = createContext<AuthContextValue>({} as AuthContextValue);
```

### Provider 外での使用を `throw Error` で検出

```typescript
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
}
```

### Context テストパターン

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

## 4. テスト戦略

### テスト種別対応表

| 対象                   | テスト種別 | ツール       | 配置場所            |
| ---------------------- | ---------- | ------------ | ------------------- |
| Utils / バリデーション | Unit       | Vitest       | `tests/unit/`       |
| Custom Hook            | Unit       | Vitest + RTL | `tests/unit/hooks/` |
| Component              | Component  | Vitest + RTL | `tests/component/`  |
| E2E クリティカルフロー | E2E        | Playwright   | `tests/e2e/`        |

### カバレッジ目標

| 種別          | Lines | Functions | Branches |
| ------------- | ----- | --------- | -------- |
| Utils / Hooks | 90%+  | 90%+      | 80%+     |
| Components    | 80%+  | 80%+      | 70%+     |
| 全体          | 80%+  | 80%+      | 70%+     |

### Next.js Hook のテスト: `vi.mock('next/navigation', ...)` 必須

`useRouter`・`useSearchParams` を使うコンポーネントのテストでは必ずモックする:

```typescript
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn(), prefetch: vi.fn() }),
  useSearchParams: () => new URLSearchParams(),
  usePathname: () => '/',
}));
```

### Component テスト: MSW で API レスポンスをモック

コンポーネントテストで API を直接呼ばない。MSW を使用する:

```typescript
import { http, HttpResponse } from 'msw';
import { server } from '@/tests/setup/server';

it('should display messages', async () => {
  server.use(
    http.get('/api/messages', () => {
      return HttpResponse.json([{ id: 1, code: 'MSG001', content: 'Hello' }]);
    })
  );

  render(<MessageList />, { wrapper: createWrapper() });
  await screen.findByText('MSG001');
});
```

詳細は [frontend/docs/TEST_STRATEGY.md](./TEST_STRATEGY.md) を参照。

---

## 5. Storybook

### 新規コンポーネントには `*.stories.tsx` 必須

新しいコンポーネントを実装したら必ず Storybook ストーリーも作成する。

### `args` と `render` の組み合わせパターン

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
    title: 'Hello',
  },
};

// Context が必要な場合は render を使用
export const WithContext: Story = {
  args: {
    title: 'Hello',
  },
  render: (_args) => (
    <MyContext.Provider value={mockContextValue}>
      <MyComponent title="Hello" />
    </MyContext.Provider>
  ),
};
```

### `pnpm type-check` で事前確認（pre-commit で失敗するため）

Storybook ストーリーを作成したら **コミット前** に必ず型チェックを実行:

```bash
cd frontend && pnpm type-check && cd ..
```

**よくある型エラー**:

- `args` プロパティが必要なのに `render` だけを指定している
- Context の型が `| undefined` を含んでいない
- Story の `render` 関数の引数型が合っていない

---

## 6. API クライアント

### `src/lib/api/generated/` を直接編集禁止

このディレクトリは Orval により自動生成される。**絶対に手動編集しない**。

### OpenAPI 変更後は `pnpm generate:api` を実行

```bash
# backend の OpenAPI を更新した後
cd backend && ./mvnw verify && cd ..
cd frontend && pnpm generate:api && cd ..
```

---

## 7. 標準タスクパターン（新規機能実装時）

新規機能を実装する Story のタスク分割テンプレート:

```
N.1  API クライアント再生成（pnpm generate:api）
N.2  カスタム Hook + 単体テスト（QueryClientProvider ラップ）
N.3  Context（必要時）+ 単体テスト（| undefined 型パターン確認）
N.4  コンポーネント + Storybook + pnpm type-check
N.5  コンポーネントテスト（MSW でモック）
N.6  E2E テスト（クリティカルフローのみ）
```

---

## 8. よくあるアンチパターン

### ❌ Context 型に `| undefined` なし

**問題**: Provider 外でコンテキストが使用された場合に実行時エラーではなく型エラーが検出されない。

**解決策**: `createContext<ContextType | undefined>(undefined)` を使用する。

### ❌ QueryClientProvider なしの Hook テスト

**問題**: React Query Hook を `QueryClientProvider` なしでテストするとエラーになる。

**解決策**: `createWrapper()` で `QueryClientProvider` をラップする（上記 §4 参照）。

### ❌ Orval 生成ファイルの直接編集

**問題**: `pnpm generate:api` を実行すると上書きされ、変更が失われる。

**解決策**: API の変更は `specs/openapi/openapi.yaml` を更新し、コード生成を再実行する。

### ❌ Next.js Hook の未モック

**問題**: `useRouter` などを使うコンポーネントのテストが `Error: invariant expected app router to be mounted` でクラッシュする。

**解決策**: テストファイルの先頭で `vi.mock('next/navigation', ...)` を追加する。

### ❌ default export の使用

**問題**: リファクタリング時の import 管理が困難になる。TypeScript の名前解決が曖昧になる。

**解決策**: すべてのコンポーネント・Hook は named export を使用する。

---

## 9. パフォーマンス

> **方針**: 明らかなボトルネックを避ける。計測なしの最適化はしない。

### React Query のキャッシュ設定

不必要な再フェッチを避けるため `staleTime` を適切に設定する:

```typescript
// ✅ 頻繁に変わらないデータはキャッシュを活用
const { data } = useGetAllMessages({
  query: {
    staleTime: 30 * 1000, // 30秒間はキャッシュを使う（再フェッチしない）
    gcTime: 5 * 60 * 1000, // 5分間メモリ保持
  },
});

// ⚠️ staleTime: 0（デフォルト）はフォーカス毎に再フェッチするため
// 静的に近いデータには設定を検討する
```

**ミューテーション後のキャッシュ無効化**:

```typescript
const mutation = useCreateMessage({
  mutation: {
    onSuccess: () => {
      // ✅ 作成後に一覧を無効化して再フェッチ
      queryClient.invalidateQueries({ queryKey: getGetAllMessagesQueryKey() });
    },
  },
});
```

### リストの `key` 設定

```tsx
// ❌ 禁止: インデックスを key にするとリスト変更時に再レンダリングが崩れる
{
  messages.map((msg, index) => <MessageRow key={index} message={msg} />);
}

// ✅ 安定した一意の ID を使う
{
  messages.map((msg) => <MessageRow key={msg.id} message={msg} />);
}
```

### `useMemo` / `useCallback` は計測してから使う

闇雲に使うと記憶コストが生じ逆効果になる:

```typescript
// ⚠️ 単純な計算に useMemo は不要（オーバーエンジニアリング）
const doubled = useMemo(() => value * 2, [value]);

// ✅ 使うべき場面: 高コストな計算、子コンポーネントへの props として渡す関数
const expensiveResult = useMemo(() => heavyComputation(data), [data]);
const handleSubmit = useCallback(() => onSubmit(formData), [onSubmit, formData]);
```

### Context の不必要な再レンダリング防止

Context の値が変わると配下の全コンポーネントが再レンダリングされる:

```typescript
// ❌ オブジェクトリテラルを毎回生成するとすべての Consumer が再レンダリング
<MyContext.Provider value={{ user, setUser }}>

// ✅ useMemo でメモ化
const value = useMemo(() => ({ user, setUser }), [user]);
<MyContext.Provider value={value}>
```

### Next.js の動的インポート

初期バンドルサイズを減らすために使用頻度の低い大きなコンポーネントを遅延ロード:

```typescript
// ✅ モーダルやダイアログなど初期表示不要なコンポーネント
const HeavyModal = dynamic(() => import('./HeavyModal'), {
  loading: () => <Skeleton />,
});
```

**使いどころ**: 重いライブラリを使うコンポーネント、初期表示で不要なモーダル・ダイアログ。

---

## 10. セキュリティ（OWASP Top 10 2025 準拠）

### A01:2025 アクセス制御の不備（Broken Access Control）

**フロントエンドの権限制御はバックエンド検証の補完**:

```typescript
// ✅ UI 制御（表示/非表示）はフロントエンドで行う
const { canCreate } = usePermission();
return <>{canCreate && <Button>作成</Button>}</>;

// ⚠️ 重要: フロントエンドの権限チェックは UX のためのもの
// 実際のアクセス制御はバックエンド API で必ず検証する
// クライアントサイドの `role` チェックは改ざんされる可能性がある
```

**直接 URL アクセスへの対策**: ページコンポーネントで認証状態を確認する:

```typescript
// ✅ 認証が必要なページでは必ずチェック
export default function AdminPage() {
  const { user, isLoading } = useAuth();

  if (isLoading) return <Loading />;
  if (!user) redirect('/login');          // 未認証はログインへ
  if (user.role !== 'ADMIN') redirect('/'); // 権限不足はトップへ

  return <AdminContent />;
}
```

---

### A02:2025 セキュリティの設定ミス（Security Misconfiguration）

**`dangerouslySetInnerHTML` の使用禁止**:

```tsx
// ❌ 禁止: XSS の原因になる
<div dangerouslySetInnerHTML={{ __html: userContent }} />

// ✅ React はデフォルトでエスケープする
<div>{userContent}</div>
```

**外部から受け取った HTML を表示する必要がある場合**: `DOMPurify` などのサニタイズライブラリを使用する（必要性自体を再検討する）。

---

### A04:2025 暗号化の失敗（Cryptographic Failures）

**アクセストークンの保存場所**:

| 保存場所           | XSS                     | CSRF       | 推奨度                            |
| ------------------ | ----------------------- | ---------- | --------------------------------- |
| `localStorage`     | 脆弱（JS から読める）   | 安全       | ⚠️ 非推奨                         |
| `sessionStorage`   | 脆弱（JS から読める）   | 安全       | ⚠️ 条件付き                       |
| `HttpOnly Cookie`  | 安全（JS から読めない） | 要CSRF対策 | ✅ 推奨                           |
| メモリ（useState） | 安全                    | 安全       | ✅ 推奨（ページリロードで消える） |

**機密データをコンソールに出力しない**:

```typescript
// ❌ 禁止: トークンや PII をログに出力
console.log('Auth token:', token);
console.log('User data:', JSON.stringify(user));

// ✅ 開発時は必要最小限の情報のみ
console.debug('Login successful for user ID:', user.id);
```

**ローカルストレージに機密情報を保存しない**:

```typescript
// ❌ 禁止: PII や機密情報を localStorage に保存
localStorage.setItem('userSSN', socialSecurityNumber);
localStorage.setItem('creditCard', cardNumber);

// ✅ セッション期間のみ必要な情報は sessionStorage か state で管理
```

---

### A05:2025 インジェクション（Injection）

**URL パラメータの安全な使用**（Open Redirect 対策）:

```typescript
// ❌ 危険: 任意の URL にリダイレクトできる
const { searchParams } = useSearchParams();
const redirectUrl = searchParams.get('redirect');
router.push(redirectUrl); // 外部サイトへのリダイレクト可能

// ✅ 同一オリジンのパスのみ許可
const redirectPath = searchParams.get('redirect') ?? '/';
const safePath =
  redirectPath.startsWith('/') && !redirectPath.startsWith('//') ? redirectPath : '/';
router.push(safePath);
```

---

### A07:2025 認証の失敗（Authentication Failures）

**トークン有効期限切れの処理**:

```typescript
// ✅ 401 レスポンスでログアウトまたはリフレッシュ処理
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // リフレッシュトークンで再取得 or ログアウト
      await logout();
      router.push('/login');
    }
    return Promise.reject(error);
  }
);
```

**ログアウト時のトークン削除**:

```typescript
// ✅ ログアウト時はクライアント側のトークンを完全に削除
function logout() {
  localStorage.removeItem('accessToken');
  sessionStorage.clear();
  queryClient.clear(); // React Query キャッシュもクリア
  router.push('/login');
}
```

---

### A09:2025 セキュリティログとアラートの失敗（Security Logging and Alerting Failures）

フロントエンドでは以下のイベントをバックエンドに送信またはモニタリングサービスに記録することを検討する:

- 認証失敗（ログイン失敗回数）
- 権限エラー（403 が多発している場合は攻撃の可能性）
- 予期しない API エラー（5xx）

**ブラウザコンソールへの機密情報出力禁止**（A04:2025 §ログの項目と同様）。

---

## 参考資料

- [Frontend テスト戦略](./TEST_STRATEGY.md)
- [frontend/CLAUDE.md](../CLAUDE.md)
- [全体テスト戦略](../../docs/quality/TEST_STRATEGY.md)
- [OWASP Top 10 2025](https://owasp.org/Top10/2025/)
- [OWASP セキュリティチェック](../../docs/quality/SECURITY.md)
