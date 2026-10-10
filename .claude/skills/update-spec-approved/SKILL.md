---
name: update-spec-approved
description: "[Human-Only] Update Issue with approved specification details and add spec-approved label after merging spec PR."
disable-model-invocation: true
---

# Issue更新と承認コマンド

## ⛔ エージェント呼び出し禁止

このスキルは **ユーザーが直接実行するスキル** です。
他のスキルやエージェント（Task ツール）から呼び出してはいけません。

**禁止理由**:
- 仕様PRのマージという外部状態変更を前提とする（マージ済みであることの確認が必要）
- GitHub Issue の更新・ラベル付与という副作用を含む
- 誤った呼び出しでレビュー未完の仕様が承認済みになる可能性がある

**エージェントがこのスキルを実行しようとした場合**:
即座に停止し、ユーザーに「このスキルはユーザー専用です。`/update-spec-approved` を直接実行してください」と報告する。

---

## 概要

仕様PRマージ後、Issueに仕様を明記し `spec-approved` ラベルを付与します。

**対応ステップ**: 6. Issue に仕様を明記 + spec-approved ラベル付与

---

## 使用方法

```bash
# Issue番号とPR番号を指定して実行
/update-spec-approved 88 102
```

---

## 前提条件

- 仕様PR（OpenAPI + 受け入れ条件）が作成済み
- PR がマージ済み（`merged` 状態）
- Issue がオープン状態である

---

## 実行フロー

### 1. PRとIssueの状態確認

```bash
# PR状態の確認
gh pr view [PR番号] --json state,merged,mergedAt

# Issue状態の確認
gh issue view [Issue番号] --json state
```

**確認項目**:

- PRが `merged` 状態であるか
- Issue が `open` 状態であるか

**エラー時**: 上記条件を満たさない場合はエラーメッセージを表示：

```
❌ PR #102 はまだマージされていません

仕様PRのレビューを完了し、マージしてから再度実行してください
```

### 2. PRから仕様情報を取得

#### 2.1 PRの変更ファイルリストを取得

```bash
gh pr view [PR番号] --json files
```

#### 2.2 仕様ファイルを特定

変更ファイルリストから以下を抽出：

- OpenAPI仕様ファイル（`specs/openapi/*.yaml`）
- 受け入れ条件ファイル（`specs/acceptance/**/*.feature`）

**例**:

```
OpenAPI仕様:
- specs/openapi/openapi.yaml

受け入れ条件:
- specs/acceptance/auth/login.feature
- specs/acceptance/auth/refresh-token.feature
- specs/acceptance/auth/logout.feature
- specs/acceptance/auth/authorization.feature
```

#### 2.3 エンドポイント情報を抽出（オプション）

OpenAPI仕様ファイルを読み込み、追加されたエンドポイントを抽出：

```bash
cat specs/openapi/openapi.yaml
```

### 3. Issueにコメントを追加

**コメント内容**:

```markdown
## ✅ 仕様承認完了

仕様 PR #[PR番号] がマージされ、仕様が確定しました。

### 承認された仕様

**仕様 PR**: #[PR番号]

**OpenAPI 仕様**:
- [`specs/openapi/openapi.yaml`](https://github.com/nagashima-toru/sandbox-claude-code/blob/master/specs/openapi/openapi.yaml)
  - `POST /api/auth/login` - ログイン
  - `POST /api/auth/refresh` - トークンリフレッシュ
  - `POST /api/auth/logout` - ログアウト

**受け入れ条件**:
- [`specs/acceptance/auth/login.feature`](https://github.com/nagashima-toru/sandbox-claude-code/blob/master/specs/acceptance/auth/login.feature)
- [`specs/acceptance/auth/refresh-token.feature`](https://github.com/nagashima-toru/sandbox-claude-code/blob/master/specs/acceptance/auth/refresh-token.feature)
- [`specs/acceptance/auth/logout.feature`](https://github.com/nagashima-toru/sandbox-claude-code/blob/master/specs/acceptance/auth/logout.feature)
- [`specs/acceptance/auth/authorization.feature`](https://github.com/nagashima-toru/sandbox-claude-code/blob/master/specs/acceptance/auth/authorization.feature)

### Next Steps

- [ ] 実装計画策定（`.epic/[YYYYMMDD]-[issue-N]-[name]/` 作成）
  ```bash
  /plan-epic [Issue番号]
  ```

- [ ] Epic ベースブランチ作成

  ```bash
  git checkout master && git pull origin master
  git checkout -b feature/issue-[N]-[name]
  git push origin feature/issue-[N]-[name]
  ```

- [ ] Story 実装開始

  ```bash
  /implement-epic [Issue番号]
  ```

```

**コマンド実行**:
```bash
gh issue comment [Issue番号] --body "[上記のMarkdown]"
```

### 4. ティアラベルの付与

Spec PR で Claude が提案し、レビューで承認されたティアをラベルとして付与する。

**AskUserQuestion でティアを確認**:

```
Spec PR で確定したティアを教えてください。

- Major: API/DB スキーマ変更・新ドメイン・外部連携変更
- Minor: 既存機能変更・機能追加（API 変更なし）
- Micro: docs・tests・configs・スキルのみ変更
```

**ラベル付与**:

```bash
# ティアに応じていずれか1つを実行
gh issue edit [Issue番号] --add-label "tier:major"
gh issue edit [Issue番号] --add-label "tier:minor"
gh issue edit [Issue番号] --add-label "tier:micro"
```

**ラベルが存在しない場合は事前に作成**:

```bash
gh label create "tier:major" --color "d73a4a" --description "Major: API/DB schema changes"
gh label create "tier:minor" --color "0075ca" --description "Minor: feature changes without API changes"
gh label create "tier:micro" --color "cfd3d7" --description "Micro: docs/tests/configs only"
```

### 5. spec-approved ラベルを付与

```bash
gh issue edit [Issue番号] --add-label "spec-approved"
```

**確認**:

```bash
gh issue view [Issue番号] --json labels
```

### 7. 結果の表示

Issue更新結果（ティア情報を含む）とNext Stepsをユーザーに表示：

```
✅ Issue #88 を更新しました

- コメントを追加しました
- spec-approved ラベルを付与しました

## 承認された仕様

**仕様 PR**: #102

**OpenAPI 仕様**:
- POST /api/auth/login - ログイン
- POST /api/auth/refresh - トークンリフレッシュ
- POST /api/auth/logout - ログアウト

**受け入れ条件**:
- specs/acceptance/auth/login.feature
- specs/acceptance/auth/refresh-token.feature
- specs/acceptance/auth/logout.feature
- specs/acceptance/auth/authorization.feature

## Next Steps

1. **実装計画策定**（ステップ7）
   ```bash
   /plan-epic 88
   ```

- .epic/ ディレクトリ作成
- requirements.md, design.md, overview.md 生成
- Story分割とタスク定義

2. **計画レビュー**（ステップ8）

   ```bash
   /review-plan 88
   ```

   - Story分割の妥当性を確認
   - 見積もりと依存関係をチェック

3. **実装開始**（ステップ9）

   ```bash
   /implement-epic 88
   ```

```

---

## エラーハンドリング

### PRがマージされていない
```

❌ PR #102 はまだマージされていません

現在の状態: open

仕様PRのレビューを完了し、マージしてから再度実行してください

```

### Issueが存在しない
```

❌ Issue #88 が見つかりません

Issue番号を確認してください

```

### Issueがクローズ済み
```

❌ Issue #88 は既にクローズされています

オープン状態のIssueに対してのみ実行できます

```

### PRが存在しない
```

❌ PR #102 が見つかりません

PR番号を確認してください

```

### 仕様ファイルが見つからない
```

⚠️ 警告: PR #102 から仕様ファイルが見つかりませんでした

以下のファイルが含まれているか確認してください：

- specs/openapi/*.yaml
- specs/acceptance/**/*.feature

仕様ファイルが含まれていない場合は、PRを修正してください

```

---

## 注意事項

- このコマンドは仕様PRが**マージされた後に実行**してください
- `spec-approved` ラベルが付与されると、`/plan-epic` コマンドが実行可能になります
- Issue番号とPR番号を間違えないように注意してください
- 仕様ファイルへのリンクは自動的にGitHubの `master` ブランチを参照します

---

## 参考資料

- [CLAUDE.md](../../CLAUDE.md) - 開発プロセス全体

---

## 関連コマンド

- `/create-spec-pr [Issue番号]` - 仕様PR作成（前のステップ）
- `/plan-epic [Issue番号]` - 実装計画策定（次のステップ）
- `/epic-status [Issue番号]` - Epic進捗確認
