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

E2E テストは Playwright で実装し、以下のクリティカルフローのみをカバーします。

### 対象フロー

- 認証（ログイン・ログアウト）
- ロールによる権限制御（ADMIN vs VIEWER）
- 主要 CRUD 操作（メッセージ一覧・作成・編集・削除）

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
| API レスポンス形式 | Backend Integration | HTTP 層の検証 |
| UI コンポーネント挙動 | Frontend Unit/Component | 高速フィードバック |
| 権限制御（API） | Backend Integration | セキュリティは必ずバックエンドで |
| 権限制御（UI 表示） | Frontend Unit + E2E | 表示ロジック + 統合確認 |
| ユーザーフロー | E2E | ブラウザ操作が必要なもののみ |

## 詳細ドキュメント

- [Backend テスト戦略](../../backend/docs/TEST_STRATEGY.md) - レイヤー別テスト・JUnit5・Testcontainers
- [Frontend テスト戦略](../../frontend/docs/TEST_STRATEGY.md) - Vitest・RTL・Playwright
