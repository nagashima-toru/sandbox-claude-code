# Create Story PR Skill

Story 実装完了後、適切なテンプレートを使用して PR を作成します。

## 目的

- Story ブランチから Epic ベースブランチへの PR を作成
- **必ず** `.github/PULL_REQUEST_TEMPLATE/story.md` テンプレートを使用
- Implementation Check の失敗を防ぐ

## 実行手順

### 1. 引数の解析

引数から Issue 番号と Story 番号を取得:

```
/create-story-pr 133 5
/create-story-pr Epic #133 Story 5
/create-story-pr issue-133 story5
```

引数がない場合はユーザーに確認:
- Issue 番号は？
- Story 番号は？

### 2. Epic ベースブランチの特定

```bash
git branch -r | grep "origin/feature/issue-[N]-" | grep -v story
```

パターン: `feature/issue-[N]-[epic-name]`

例:
- Issue #133 → `feature/issue-133-permission-ui`
- Issue #88 → `feature/issue-88-auth`

見つからない場合はエラーを返す。

### 3. Story ブランチの取得

```bash
git branch --show-current
```

現在のブランチを Story ブランチとして使用。

### 4. PR テンプレートの確認

`.github/PULL_REQUEST_TEMPLATE/story.md` が存在することを確認。

### 5. PR 作成

**IMPORTANT**: 必ず `--template` オプションを使用

```bash
gh pr create \
  --base feature/issue-[N]-[epic-name] \
  --head feature/issue-[N]-[epic-name]-story[M] \
  --template .github/PULL_REQUEST_TEMPLATE/story.md
```

### 6. 結果の報告

PR URL を返し、次のステップを案内:

```
✓ PR created successfully!
  URL: https://github.com/.../pull/142

Next steps:
  1. Fill in the PR template (especially "このPRで検証した受け入れ条件")
  2. Wait for CI checks to pass
  3. Request review
```

## 使用例

### コマンドライン引数

```bash
/create-story-pr 133 5
```

### 対話形式

```bash
/create-story-pr

> Issue 番号は？
133
> Story 番号は？
5
```

## エラーハンドリング

| エラー | 原因 | 対処法 |
|--------|------|--------|
| Epic base branch not found | Epic ブランチが remote に存在しない | Epic ブランチを push するか、正しい Issue 番号を指定 |
| PR template not found | `.github/PULL_REQUEST_TEMPLATE/story.md` が存在しない | テンプレートファイルを作成 |
| gh CLI not installed | gh コマンドが見つからない | `brew install gh` で gh CLI をインストール |

## 注意事項

- **必ず** `--template` オプションを使用（Implementation Check が失敗するため）
- Epic ベースブランチは `feature/issue-[N]-[epic-name]` パターンに従う必要がある
- Story ブランチは `feature/issue-[N]-[epic-name]-story[M]` パターンを推奨
- PR 作成後、テンプレートの空欄を埋める必要がある

## 参考

- **テンプレートファイル**: `.github/PULL_REQUEST_TEMPLATE/story.md`
- **Epic Documents**: `docs/development/EPIC_DOCUMENTS.md`
- **Git Workflow**: `docs/development/GIT_WORKFLOW.md`
