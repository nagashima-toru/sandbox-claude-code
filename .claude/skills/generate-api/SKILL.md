---
name: generate-api
description: Regenerate TypeScript API client from OpenAPI specification after backend API changes. Run after modifying backend/src/main/resources/api/openapi.yaml.
---

# API クライアント生成スキル

バックエンドの OpenAPI 仕様変更後に TypeScript API クライアントを再生成します。

## 手順

### 1. バックエンドの OpenAPI 仕様を更新・確認

```bash
# バックエンドでビルド検証（OpenAPI 仕様を再生成）
cd backend && ./mvnw verify && cd ..
```

### 2. フロントエンドの API クライアントを再生成

```bash
cd frontend && pnpm generate:api && cd ..
```

生成先: `frontend/src/lib/api/generated/`

### 3. TypeScript 型チェックで変更を確認

```bash
cd frontend && pnpm type-check && cd ..
```

型エラーが発生した場合、API コントラクトの変更に合わせてコンポーネントを修正します。

## 重要なルール

### 生成ファイルは編集禁止

`src/lib/api/generated/` 以下のファイルは **直接編集しない**。
次回の生成で上書きされます。カスタム動作が必要な場合は別ファイルに拡張します。

```typescript
// ✅ 生成されたフックを使用
import { useGetAllMessages } from '@/lib/api/generated/messages';

// ❌ 手動で axios を呼び出さない
import axios from 'axios';
axios.get('/api/messages');
```

### 生成ファイルは .gitignore に追加済み

`src/lib/api/generated/` は gitignore 済み（マージコンフリクトなし、リポジトリサイズ最小化）。

## 設定ファイル

**`frontend/orval.config.ts`**:

```typescript
import { defineConfig } from 'orval';

export default defineConfig({
  api: {
    input: '../backend/src/main/resources/api/openapi.yaml',
    output: {
      target: './src/lib/api/generated/messages.ts',
      client: 'react-query',
      httpClient: 'axios',
      mode: 'single',
      override: {
        mutator: {
          path: './src/lib/api/client.ts',
          name: 'customInstance',
        },
      },
    },
  },
});
```

## 生成されるコード

- **型定義**: OpenAPI スキーマから TypeScript interface を生成
- **API 関数**: エンドポイントごとの型付き関数
- **React Query フック**: `useGetXxx`（クエリ）、`useCreateXxx`（ミューテーション）など

## コンポーネントでの使用例

```typescript
import { useGetAllMessages, useDeleteMessage } from '@/lib/api/generated/messages';

export function MessageTable() {
  const { data: messages, isLoading } = useGetAllMessages();
  const deleteMutation = useDeleteMessage({
    onSuccess: () => queryClient.invalidateQueries(['getAllMessages']),
  });
  // ...
}
```

## ワークフロー

```
OpenAPI 仕様変更（backend/src/main/resources/api/openapi.yaml）
         ↓
  ./mvnw verify（バックエンドビルド確認）
         ↓
  pnpm generate:api（フロントエンド）
         ↓
  TypeScript 型チェック
         ↓
  型エラーがあればコンポーネントを修正
```
