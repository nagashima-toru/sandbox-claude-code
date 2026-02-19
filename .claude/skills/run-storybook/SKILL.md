---
name: run-storybook
description: Start Storybook development server for UI component development and verification. Use when developing new components or reviewing existing stories.
---

# Storybook 実行スキル

UI コンポーネントの開発・検証時に Storybook を起動します。

## 起動コマンド

```bash
# Storybook 開発サーバー起動（ポート 6006）
cd frontend && pnpm storybook && cd ..

# または scripts を使用
./scripts/storybook.sh
```

アクセス: <http://localhost:6006>

## ストーリーファイルの配置ルール

ストーリーはコンポーネントと **co-locate**（同じディレクトリに配置）する。

```
src/components/
├── ui/
│   ├── button.tsx
│   └── button.stories.tsx       # ✅ コンポーネントと同じ場所
├── common/
│   ├── Loading.tsx
│   └── Loading.stories.tsx
└── messages/
    ├── MessageTable.tsx
    └── MessageTable.stories.tsx
```

**NG**: `src/stories/button.stories.tsx` のような別ディレクトリへの配置。

## ストーリーファイルの命名規則

- コンポーネント: `ComponentName.tsx`（PascalCase）
- ストーリー: `ComponentName.stories.tsx`（同じ接頭辞に `.stories.tsx`）

**サイドバー階層**:

- UI コンポーネント: `UI/ComponentName`
- 共通コンポーネント: `Common/ComponentName`
- ドメイン固有: `Messages/ComponentName` など

## ストーリーの基本構造

```typescript
import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import { Button } from './button';

const meta = {
  title: 'UI/Button',
  component: Button,
  tags: ['autodocs'],
  argTypes: {
    variant: { control: 'select', options: ['default', 'destructive'] },
  },
} satisfies Meta<typeof Button>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = { args: { children: 'Button' } };
export const Destructive: Story = { args: { variant: 'destructive', children: 'Delete' } };
```

## MSW による API モックパターン

### グローバルハンドラー（`src/mocks/handlers.ts`）

```typescript
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('http://localhost:8080/api/messages', () => {
    return HttpResponse.json([
      { id: 1, code: 'MSG001', content: 'Test message 1' },
    ]);
  }),
];
```

### ストーリーごとのオーバーライド

```typescript
export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [
        http.get('http://localhost:8080/api/messages', async () => {
          await new Promise((resolve) => setTimeout(resolve, 3000));
          return HttpResponse.json([]);
        }),
      ],
    },
  },
};

export const ErrorState: Story = {
  parameters: {
    msw: {
      handlers: [
        http.get('http://localhost:8080/api/messages', () => {
          return HttpResponse.json({ message: 'Internal Server Error' }, { status: 500 });
        }),
      ],
    },
  },
};
```

## ストーリーのベストプラクティス

1. **Args を使う**（ハードコードしない）: `args: { variant: 'default' }` ✅
2. **記述的な名前**: `DisabledState` ✅ / `Story1` ❌
3. **MSW でモック**: API をハードコードしない
4. **多様な状態をカバー**: Default / Loading / Error / Empty / Disabled / Edge cases

## トラブルシューティング

| 症状 | 解決策 |
|-----|--------|
| 起動しない | `pnpm storybook --no-manager-cache` でキャッシュクリア |
| MSW が動かない | `src/mocks/handlers.ts` の存在確認、URL が一致しているか確認 |
| ストーリーが表示されない | ファイル名 `*.stories.tsx` を確認、Storybook を再起動 |
| 型エラー | `@storybook/nextjs-vite` の型をインストール確認 |
