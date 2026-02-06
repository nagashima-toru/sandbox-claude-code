# Epic状態確認コマンド

## 概要

Epic の進捗状況を確認し、次に実装すべきStoryを提案します。

**対応ステップ**: いつでも（進捗確認）

---

## 使用方法

```bash
# Issue番号を指定して実行
/epic-status 88

# 引数なしで実行（Epic選択UI）
/epic-status
```

---

## 実行フロー

### 1. Epic の選択

#### 引数あり: 指定されたEpicの状態を表示

Issue番号（または識別子）が引数で渡された場合：
1. `.epic/` ディレクトリから該当するEpicを検索
2. 見つかったEpicの状態を表示

**検索パターン**:
- `88` → `*-issue-88-*/`
- `issue-88` → `*-issue-88-*/`
- `88-auth` → `*-88-auth/`

#### 引数なし: Epic選択UI

引数が渡されない場合：
1. `.epic/` ディレクトリ内の全Epicをスキャン
2. 各Epicの `overview.md` から以下を取得：
   - Issue番号
   - Epicタイトル
   - Story進捗（完了/全体）
   - 最終更新日時
3. AskUserQuestion で選択肢を表示：
   ```
   確認するEpicを選択してください

   選択肢:
   - Epic #88: 認証・認可機能 (Story 6/7 完了、最終更新: 2026-02-04)
   - Epic #92: 新機能XYZ (Story 0/5 完了、最終更新: 2026-02-05)
   ```

### 2. Epic情報の読み込み

#### 2.1 overview.md の読み込み

```bash
cat .epic/[YYYYMMDD]-[issue-N]-[title]/overview.md
```

**抽出する情報**:
- Epic概要（目的、完了条件）
- Story構成（Story一覧、見積もり）
- 依存関係
- 進捗状況（各StoryのTask完了状況）

#### 2.2 各Story の tasklist.md を読み込む

未完了のStoryについて、タスク詳細を読み込む：
```bash
cat .epic/[YYYYMMDD]-[issue-N]-[title]/story[N]-[name]/tasklist.md
```

**抽出する情報**:
- Task完了状況（チェックボックス）
- 見積もり時間
- 実績時間
- 進捗メモ

### 3. 進捗レポートの生成

#### 3.1 全体進捗の計算

**Story進捗率**:
```
完了Story数 / 全Story数 × 100
```

**Task進捗率**:
```
完了Task数 / 全Task数 × 100
```

**時間進捗**:
```
実績時間 / 見積もり時間 × 100
```

#### 3.2 Story別進捗の集計

各Storyについて：
- タイトルと状態（✅完了 / 🚧進行中 / ⏱️未着手）
- Task完了状況（例: 3/5タスク完了）
- 見積もり時間と実績時間
- PR番号（完了Story）

### 4. GitHubのIssue/PR状態を確認（オプション）

```bash
# Issueの状態
gh issue view [Issue番号] --json state,labels

# 関連PRの状態
gh pr list --head feature/issue-[N]-* --json number,title,state
```

### 5. 次のStoryを提案

**提案ロジック**:
1. 未完了のStoryを依存関係順にソート
2. 依存するStoryがすべて完了しているStoryを抽出
3. 最初のStoryを推奨として提案

**依存関係の確認**:
- `overview.md` の依存関係図を解析
- 依存するStoryが完了していない場合は警告

### 6. 進捗レポートの表示

```
# Epic #88: 認証・認可機能

## Epic 概要

**目的**: JWT ベースの認証と RBAC を実装し、API のセキュリティを確保する

**完了条件**:
- 管理者（ADMIN）と閲覧者（VIEWER）の2つのロールで認証・認可が動作する
- すべての Message API エンドポイントが認証必須となる
- フロントエンドからログイン・ログアウトができる

---

## 全体進捗

- **Story進捗**: 6/7 完了 (85.7%)
- **Task進捗**: 24/26 完了 (92.3%)
- **時間進捗**: 実績 15.5h / 見積もり 16.5h (93.9%)

**進捗バー**:
```
Story: ████████████████░░ 85.7%
Task:  █████████████████░ 92.3%
Time:  █████████████████░ 93.9%
```

---

## Story 別進捗

### ✅ Story 1: ユーザー管理基盤
- **状態**: 完了
- **Task**: 4/4 完了
- **時間**: 実績 2.0h / 見積もり 2.0h
- **PR**: #104 (merged)

### ✅ Story 2: JWT 認証基盤
- **状態**: 完了
- **Task**: 5/5 完了
- **時間**: 実績 3.2h / 見積もり 3.25h
- **PR**: #105 (merged)

### ✅ Story 3: 認証エンドポイント
- **状態**: 完了
- **Task**: 4/4 完了
- **時間**: 実績 3.3h / 見積もり 3.25h
- **PR**: #106 (merged)

### ✅ Story 4: 認可機能
- **状態**: 完了
- **Task**: 3/3 完了
- **時間**: 実績 1.8h / 見積もり 1.75h
- **PR**: #107 (merged)

### ✅ Story 5: OpenAPI 仕様更新
- **状態**: 完了
- **Task**: 2/2 完了
- **時間**: 実績 1.0h / 見積もり 1.0h
- **PR**: #108 (merged)

### ✅ Story 6: フロントエンド対応
- **状態**: 完了
- **Task**: 6/6 完了
- **時間**: 実績 3.7h / 見積もり 3.75h
- **PR**: #109 (merged)

### 🚧 Story 7: 結合テスト（進行中）
- **状態**: 進行中
- **Task**: 0/2 完了
- **時間**: 実績 0.5h / 見積もり 1.5h
- **残タスク**:
  - [ ] Task 7.1: ローカル CI チェックの実行
  - [ ] Task 7.2: E2E テストの作成（オプション）

---

## 次に実装すべき Story

**推奨**: Story 7（結合テスト）

**理由**:
- すべての依存Storyが完了している
- 最後のStoryであり、完了すればEpicが完成する

**実装コマンド**:
```bash
/implement-story 88 7
```

または Epic全体を完成させる：
```bash
/implement-epic 88
```

---

## GitHub Issue/PR 状態

**Issue #88**: open
- ラベル: epic, spec-approved
- 最終更新: 2026-02-04

**関連PR**:
- #104: Story 1（merged）
- #105: Story 2（merged）
- #106: Story 3（merged）
- #107: Story 4（merged）
- #108: Story 5（merged）
- #109: Story 6（merged）

---

## 最終確認（全Story完了後）

- [ ] 全Storyが完了している
- [ ] 全PRがマージされている
- [ ] 結合テストが通過している
- [ ] Epic PR の準備ができている

**Epic PR 作成**:
```bash
gh pr create --base master \
             --head feature/issue-88-auth \
             --template .github/PULL_REQUEST_TEMPLATE/epic.md
```
```

---

## エラーハンドリング

### Epicが存在しない
```
❌ Epic #88 が見つかりません

.epic/ ディレクトリに該当するEpicが存在しません。
以下のコマンドで実装計画を策定してください：
/plan-epic 88
```

### overview.md が読めない
```
❌ .epic/20260207-88-auth/overview.md が読み込めません

ファイルが破損しているか、権限がない可能性があります
```

### .epic/ が空
```
❌ .epic/ ディレクトリが空です

実装計画を策定してから実行してください：
/plan-epic [Issue番号]
```

---

## 注意事項

- このコマンドは **いつでも実行可能** です（実装中の進捗確認に便利）
- `.epic/` ディレクトリの情報を参照するため、ローカル環境での実行が前提です
- GitHub Issue/PR の状態確認はオプションです（ネットワーク接続が必要）
- 進捗レポートは Markdown 形式で表示されます

---

## 進捗の更新タイミング

Epic Documentsの進捗を最新に保つため、以下のタイミングで更新してください：

**Task完了時**:
- `tasklist.md` のチェックボックスを更新

**Story完了時**:
- `tasklist.md` の進捗セクションを更新
- `overview.md` の該当Storyにチェック

詳細は [Epic Documents](../../docs/development/EPIC_DOCUMENTS.md) の「更新タイミング」セクションを参照。

---

## 参考資料

- [Epic Documents](../../docs/development/EPIC_DOCUMENTS.md) - Epic構造と更新タイミング
- [CLAUDE.md](../../CLAUDE.md) - 開発プロセス全体

---

## 関連コマンド

- `/plan-epic [Issue番号]` - 実装計画策定
- `/implement-epic [Issue番号]` - Epic全体の実装
- `/implement-story [Issue番号] [Story番号]` - 個別Storyの実装
- `/review-plan [Issue番号]` - 計画レビュー
