# テスト戦略（全体）

このドキュメントはシステム全体のテスト方針を定義します。
バックエンド・フロントエンドそれぞれの詳細は各サブディレクトリのドキュメントを参照してください。

## テストピラミッド（全体）

```
           /\
          /  \  E2E Tests (5%)
         /    \  Playwright
        /______\
       /        \  Integration Tests (25%)
      /  Backend  \  Testcontainers / MockMvc
     /______________\
    /                \
   /   Unit Tests     \ (70%)
  / Backend + Frontend \  Vitest / JUnit5
 /______________________\
```

| 種別 | 比率 | ツール | 目的 |
|------|------|--------|------|
| Unit | 70% | JUnit 5 / Vitest | ロジック・コンポーネント単体 |
| Integration | 25% | Testcontainers / MockMvc | レイヤー間結合・DB |
| E2E | 5% | Playwright | クリティカルなユーザーフロー |

## カバレッジ目標

| 対象 | Line | Branch | 詳細 |
|------|------|--------|------|
| Backend 全体 | 80%+ | 75%+ | `./mvnw test jacoco:report` |
| Frontend 全体 | 80%+ | 70%+ | `pnpm test:coverage` |

## E2E テスト方針

E2E テストは Playwright で実装し、**バックエンド・フロントエンド・ブラウザの3層が同時に関与するクリティカルフロー** のみをカバーします。

### E2E テスト選定基準

以下の質問に全て「Yes」と答えられる場合のみ E2E テストとして実装する:

1. **Q1**: バックエンド API とフロントエンドの両方が同時に関与するフローか？
2. **Q2**: Component テストや Backend 統合テストで代替できないか？（「できない」場合のみ E2E）
3. **Q3**: 既存 E2E テストと実質同一のフローではないか？
4. **Q4**: 追加後も時間予算（CI 5分以内・テスト数 30件以下）に収まるか？

### 対象フロー

- 認証（ログイン・ログアウト・リダイレクト）
- 主要 CRUD 操作（メッセージ作成・編集・削除）
- ADMIN/VIEWER ロール別の操作フロー

### E2E にしてはいけないパターン（禁止）

以下は Unit/Component/Backend 統合テストで対応すること:

| 検証内容 | 適切なテスト種別 | 理由 |
|---------|----------------|------|
| フォームバリデーション（Zod スキーマ） | Unit テスト | ブラウザ不要 |
| ボタンの表示/非表示（`usePermission` フック） | Unit テスト | フック単体で検証可能 |
| レスポンシブレイアウト | Component テスト（`matchMedia` モック） | CSS 検証はブラウザ不要 |
| i18n テキスト表示 | Component テスト（LocaleContext モック） | テキスト表示のみ |
| localStorage の読み書き | Unit テスト（jsdom） | DOM API で十分 |
| debounce の動作 | Unit テスト（`vi.useFakeTimers()`） | タイマー制御で十分 |
| HTTP エラーレスポンス形式（403/401 等） | Backend MockMvc 統合テスト | Backend 責務 |
| 既存 E2E テストと実質同一のフロー | 削除または既存テストで代替 | 重複排除 |

### 時間予算

- **上限件数**: 30テスト以下（CI 5分以内を目安）
- **超過時の対応**: 追加する前に削減対象を検討する

### 実行環境

```bash
# フル E2E 環境（バックエンド + フロントエンド起動が必要）
./scripts/e2e-test-local.sh

# または手動
docker compose up postgres -d
cd backend && ./mvnw spring-boot:run &
cd frontend && pnpm test:e2e
```

### API 直接呼び出しテスト

ブラウザ操作を伴わない API レベルの検証には Playwright の `request` fixture を使用します。

```typescript
test('VIEWER は POST /api/messages で 403 を受け取る', async ({ page, request }) => {
  await login(page, 'viewer', 'viewer123');
  const token = await page.evaluate(() => localStorage.getItem('accessToken'));

  const response = await request.post('http://localhost:8080/api/messages', {
    headers: { Authorization: `Bearer ${token}` },
    data: { code: 'TEST', content: 'Test' },
  });

  expect(response.status()).toBe(403);
});
```

## テスト分担方針

| 検証内容 | 担当 | 理由 |
|---------|------|------|
| ビジネスロジック | Backend Unit | ドメイン層が正 |
| API レスポンス形式 | Backend Integration（MockMvc） | HTTP 層の検証 |
| 403/401 等の HTTP エラー | Backend Integration（MockMvc） | Backend 責務・ブラウザ不要 |
| UI コンポーネント挙動 | Frontend Unit/Component | 高速フィードバック |
| フォームバリデーション | Frontend Unit（Zod スキーマ） | ブラウザ不要 |
| 権限制御（API） | Backend Integration | セキュリティは必ずバックエンドで |
| 権限制御（UI 表示） | Frontend Unit（usePermission フック） | フック単体で検証可能 |
| ユーザーフロー（3層関与） | E2E | ブラウザ操作が必要なもののみ |

## 詳細ドキュメント

- [Backend テスト戦略](../../backend/docs/TEST_STRATEGY.md) - レイヤー別テスト・JUnit5・Testcontainers
- [Frontend テスト戦略](../../frontend/docs/TEST_STRATEGY.md) - Vitest・RTL・Playwright
