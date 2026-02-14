# Git Workflow (Epic-based Development)

Epic Documents に基づく開発では、以下のブランチ戦略を採用します。

---

## ブランチ構成

```
master (main branch)
  └── feature/issue-[N]-[epic-name]  ← Epic のベースブランチ
       ├── feature/issue-[N]-[epic-name]-spec    ← 仕様PR (OpenAPI + 受け入れ条件)
       ├── feature/issue-[N]-[epic-name]-story1  ← Story 実装
       ├── feature/issue-[N]-[epic-name]-story2
       ├── feature/issue-[N]-[epic-name]-story3
       └── ...
```

**例（認証・認可機能）:**

```
master
  └── feature/issue-88-auth
       ├── feature/issue-88-auth-spec    (仕様定義: OpenAPI + 受け入れ条件)
       ├── feature/issue-88-auth-story1  (ユーザー管理基盤)
       ├── feature/issue-88-auth-story2  (JWT認証基盤)
       ├── feature/issue-88-auth-story3  (認証エンドポイント)
       └── ...
```

---

## ワークフロー

### 0. 仕様 PR 作成（Epic 開始前）

```bash
# Epic ベースブランチを作成
git checkout master
git pull
git checkout -b feature/issue-88-auth
git push -u origin feature/issue-88-auth

# 仕様ブランチを作成
git checkout -b feature/issue-88-auth-spec

# OpenAPI仕様と受け入れ条件を追加
# ...編集作業...

# 仕様PRを Epic ベースブランチに向けて作成
gh pr create --base feature/issue-88-auth \
             --head feature/issue-88-auth-spec \
             --template .github/PULL_REQUEST_TEMPLATE/spec.md \
             --label spec

# レビュー・承認後、Epic ベースブランチにマージ
gh pr merge --merge

# Issue に spec-approved ラベル付与
gh issue edit 88 --add-label spec-approved
```

**重要**: 仕様PRは master ではなく **Epic ベースブランチ** にマージします。これにより、master のビルドを保護できます（OpenAPI 仕様は既存 Controller に実装を強制するため）。

---

### 1. Epic 開始時（仕様承認後）

Epic ベースブランチは仕様PR作成時に既に作成済みです。

---

### 2. Story 開始時

```bash
# epic ベースブランチから Story ブランチを作成
git checkout feature/issue-88-auth
git pull
git checkout -b feature/issue-88-auth-story1
git push -u origin feature/issue-88-auth-story1
```

---

### 3. Story 完了時（PR 作成）

```bash
# Story ブランチ → epic ベースブランチへ PR
gh pr create --base feature/issue-88-auth \
             --head feature/issue-88-auth-story1 \
             --template .github/PULL_REQUEST_TEMPLATE/story.md

# PR タイトル例: "[Story 1] ユーザー管理基盤の構築"
```

**PR 本文の必須項目:**

- `Story: #[Issue番号]` （例: `Story: #88`）
- 変更内容の記載
- 完了したタスクのチェックリスト

---

### 4. 全 Story 完了後（Epic PR: Epic branch → master）

```bash
# Epic ベースブランチ → master へ PR
gh pr create --base master \
             --head feature/issue-88-auth \
             --template .github/PULL_REQUEST_TEMPLATE/epic.md

# PR タイトル例: "[Epic #88] 認証・認可機能の実装"
```

**PR 本文の必須項目:**

- `Closes #[Issue番号]` （例: `Closes #88`）
- 全 Story の完了確認
- テスト実施結果
- 仕様 PR への参照

**この Epic PR には以下が含まれます:**

- 仕様 (OpenAPI + 受け入れ条件)
- 全 Story の実装
- すべてのテストコード

これにより、master は常にビルド可能な状態を保ちます。

---

## 並行作業のパターン

### パターン A: 依存関係がない Story の並行作業

```
feature/issue-88-auth
  ├── feature/issue-88-auth-story1  (並行作業 OK)
  └── feature/issue-88-auth-story5  (並行作業 OK)
```

**条件**: Story 5 が Story 1 に依存していない場合

---

### パターン B: 前 Story 完了後に次 Story を開始

```
feature/issue-88-auth
  ├── feature/issue-88-auth-story1  (完了)
  └── feature/issue-88-auth-story2  (Story 1 マージ後に開始)
```

**条件**: Story 2 が Story 1 に依存している場合

---

## PR テンプレート

GitHub PR テンプレートを使用して、一貫性のある PR を作成します。

| テンプレート | 用途 | パス |
|-------------|------|------|
| `story.md` | Story ブランチ → epic ベース | `.github/PULL_REQUEST_TEMPLATE/story.md` |
| `epic.md` | epic ベース → master | `.github/PULL_REQUEST_TEMPLATE/epic.md` |

**テンプレートの使用方法:**

```bash
# Story PR
gh pr create --template .github/PULL_REQUEST_TEMPLATE/story.md

# 最終 PR
gh pr create --template .github/PULL_REQUEST_TEMPLATE/epic.md
```

---

## Implementation Check との関係

`.github/workflows/implementation-check.yml` は以下をチェックします：

1. **仕様承認チェック**: PR 本文に `Story: #xxx` または `Closes #xxx` の記載が必須
2. **ラベル確認**: 親 Issue に `spec-approved` ラベルが必要（`security`, `ci-cd`, `refactoring` の場合はスキップ）

**重要**: Issue に `security`, `ci-cd`, `refactoring` ラベルがある場合、仕様承認チェックは自動的にスキップされます。

---

## ブランチ命名規則

```
feature/issue-[IssueNumber]-[epic-name]-story[N]
```

**例:**

- `feature/issue-88-auth` (epic ベース)
- `feature/issue-88-auth-story1` (Story 1)
- `feature/issue-88-auth-story2` (Story 2)
