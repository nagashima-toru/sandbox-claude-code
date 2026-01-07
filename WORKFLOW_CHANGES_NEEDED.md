# ワークフローファイルの変更が必要

GitHub App の権限制限により、ワークフローファイルを自動的に更新できませんでした。
以下の変更を `.github/workflows/frontend-ci.yml` に手動で適用してください。

## 必要な変更

各ジョブ（lint、type-check、test、build、e2e）の「Install dependencies」ステップの**後**に、
以下のステップを追加する必要があります：

```yaml
      - name: Generate API client
        run: pnpm generate:api
```

## 具体的な追加位置

### 1. lint ジョブ（行36-37の後）
```yaml
      - name: Install dependencies
        run: pnpm install --frozen-lockfile

      - name: Generate API client
        run: pnpm generate:api

      - name: Run ESLint
        run: pnpm lint
```

### 2. type-check ジョブ（行66-67の後）
```yaml
      - name: Install dependencies
        run: pnpm install --frozen-lockfile

      - name: Generate API client
        run: pnpm generate:api

      - name: Run TypeScript type check
        run: pnpm type-check
```

### 3. test ジョブ（行93-94の後）
```yaml
      - name: Install dependencies
        run: pnpm install --frozen-lockfile

      - name: Generate API client
        run: pnpm generate:api

      - name: Run tests with coverage
        run: pnpm test -- --coverage
```

### 4. build ジョブ（行128-129の後）
```yaml
      - name: Install dependencies
        run: pnpm install --frozen-lockfile

      - name: Generate API client
        run: pnpm generate:api

      - name: Build Next.js application
        run: pnpm build
```

### 5. e2e ジョブ（行164-165の後）
```yaml
      - name: Install dependencies
        run: pnpm install --frozen-lockfile

      - name: Generate API client
        run: pnpm generate:api

      - name: Install Playwright browsers
        run: pnpm exec playwright install --with-deps
```

## 理由

フロントエンドのコードは、バックエンドの OpenAPI 仕様から生成される API クライアントコード（`@/lib/api/generated/*`）に依存しています。このため、テスト・ビルド・型チェックを実行する前に `pnpm generate:api` を実行する必要があります。

## 適用方法

```bash
# 1. ワークフローファイルを編集
vim .github/workflows/frontend-ci.yml

# 2. 上記の変更を5箇所すべてに適用

# 3. コミット
git add .github/workflows/frontend-ci.yml
git commit -m "ci: ワークフローに API 生成ステップを追加"

# 4. プッシュ
git push
```
