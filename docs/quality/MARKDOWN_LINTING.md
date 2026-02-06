# Markdown Linting

## Overview

このプロジェクトでは、Markdownドキュメントの品質を保つため、markdownlint-cli2を使用した自動検証を導入しています。

## Quick Start

### ローカル検証

```bash
# Markdown linting実行
cd frontend
pnpm lint:md

# 自動修正
pnpm lint:md:fix
```

### CI検証

- PRを作成すると、自動的にMarkdown lintingが実行されます
- 検証結果はGitHub ActionsのSummaryで確認できます

## ルール設定

設定ファイル: `.markdownlint.jsonc`

### 主要なルール

- **MD013**: 行長は120文字まで（コードブロック・テーブルは除外）
- **MD033**: 一部のHTMLタグ（`<details>`, `<summary>`など）は許可
- **MD041**: ドキュメント先頭の見出しは必須（Epicドキュメントは除外）
- **MD046**: コードブロックはfenced style（` ``` `）を使用

### ディレクトリ別のルール

| ディレクトリ | 厳密度 | 特徴 |
|-------------|--------|------|
| `docs/` | 高 | 技術ドキュメント、厳密な構文 |
| `.epic/` | 中 | Epic計画書、タスクリスト対応 |
| `.claude/` | 柔軟 | カスタムコマンド説明 |
| `.github/PULL_REQUEST_TEMPLATE/` | 高 | PRテンプレート、統一性重視 |

## よくある問題と解決方法

### MD013: Line too long

**問題**: 行が120文字を超えています

**解決方法**:

- 文章を適切に改行
- テーブル・コードブロックは自動的に除外されます

### MD033: Inline HTML

**問題**: HTMLタグが使用されています

**解決方法**:

- 許可されているタグ（`<details>`, `<summary>`など）は使用可能
- それ以外はMarkdown記法に置き換える

### MD040: Fenced code blocks should have a language specified

**問題**: コードブロックに言語が指定されていません

**解決方法**:

```markdown
# 悪い例
\`\`\`
code here
\`\`\`

# 良い例
\`\`\`bash
code here
\`\`\`
```

## 自動修正の制限

`pnpm lint:md:fix` で自動修正できる問題:

- リストのインデント
- 見出しの前後の空行
- 末尾の空行

自動修正できない問題（手動修正が必要）:

- 見出し階層のスキップ（H1→H3）
- リンク切れ
- 不適切なHTMLタグ

## トラブルシューティング

### CIでのみ失敗する

1. ローカルで再現:

   ```bash
   cd frontend
   pnpm lint:md
   ```

2. 詳細なエラーメッセージを確認:

   ```bash
   pnpm lint:md --verbose
   ```

### 特定のルールを無効化したい場合

ファイル内でコメントを使用:

```markdown
<!-- markdownlint-disable MD013 -->
This line can be very long without triggering MD013 error.
<!-- markdownlint-enable MD013 -->
```

## 参考資料

- [markdownlint Rules](https://github.com/DavidAnson/markdownlint/blob/main/doc/Rules.md)
- [markdownlint-cli2 Documentation](https://github.com/DavidAnson/markdownlint-cli2)
