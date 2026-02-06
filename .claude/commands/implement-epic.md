# Epic 実装ガイド

## Epic の選択

### 引数あり: 指定された Epic を実装

コマンド実行時に Epic 名（例: `88`, `issue-88`, `88-auth`）が渡された場合：

1. `.epic/` ディレクトリから該当する Epic を検索
2. 見つかった Epic の実装を開始

### 引数なし: Epic を選択

引数が渡されない場合：

1. `.epic/` ディレクトリ内の全 Epic をスキャン
2. 各 Epic の `overview.md` から以下を取得：
   - Issue 番号
   - Epic タイトル
   - 完了/未完了の Story 数
3. AskUserQuestion で選択肢を表示：

   ```
   実装する Epic を選択してください

   選択肢:
   - Epic #88: 認証・認可機能 (Story 6/7 完了)
   - Epic #92: 新機能XYZ (Story 0/5 完了)
   ```

4. ユーザーが選択した Epic で実装を開始

## 実行前の必須確認

1. **Epic 状況の確認**
   - `.epic/[日付]-[issue番号]-[epic名]/overview.md` を読む
   - 未完了の Story を特定する
   - 次に実装すべき Story を確認する

2. **現在のブランチ確認**

   ```bash
   git branch --show-current
   git status
   ```

   - 現在 Epic ベースブランチ（例: `feature/issue-88-auth`）にいることを確認
   - Working tree が clean であることを確認

## 実装フロー

### Phase 1: Story の開始

1. **Story ディレクトリの確認**
   - `.epic/[日付]-[issue番号]-[epic名]/story[N]-[name]/tasklist.md` を読む
   - 全タスクの内容と受け入れ条件を理解する

2. **Story ブランチの作成**

   ```bash
   git checkout -b feature/issue-[N]-[epic-name]-story[X]
   ```

3. **TaskCreate でタスク管理開始**
   - tasklist.md の各タスクを TaskCreate で登録
   - 見積もり時間も記録

### Phase 2: タスクの実装

**各タスクごとに以下を実行：**

1. **タスク開始**

   ```
   TaskUpdate taskId=[id] status=in_progress
   ```

2. **実装**
   - CLAUDE.md の開発ガイドラインに従う
   - Backend: Clean Architecture、JUnit テスト必須
   - Frontend: Functional components、named exports、テスト・Storybook 必須

3. **ローカルテスト**
   - Backend: `cd backend && ./mvnw test`
   - Frontend: `cd frontend && pnpm test`
   - 全テストが通ることを確認

4. **セルフレビューの実施と記録**
   - 実装内容を確認
   - 重要な指摘事項を抽出
   - `.epic/[日付]-[issue番号]-[epic名]/story[N]-[name]/self-review-task[N].[M].md` に記録
   - 必要に応じて修正を実施

5. **tasklist.md の更新**
   - 完了条件のチェックボックスを更新
   - 実績時間とメモを記録

6. **タスク完了**

   ```
   TaskUpdate taskId=[id] status=completed
   ```

7. **コミット**

   ```bash
   git add [変更ファイル]
   git commit -m "[type]: [タスクの説明] (#[issue番号])

   [詳細な変更内容]

   Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
   ```

### Phase 3: Story の完了

**全タスク完了後：**

1. **overview.md の更新**
   - `.epic/[日付]-[issue番号]-[epic名]/overview.md` の該当 Story に ✅ マーク

2. **最終確認**
   - [ ] 全タスクが完了している
   - [ ] 全テストが通過している
   - [ ] セルフレビューが全て記録されている
   - [ ] tasklist.md の進捗が更新されている

3. **ブランチのプッシュ**

   ```bash
   git push origin feature/issue-[N]-[epic-name]-story[X]
   ```

4. **PR 作成**（必須: テンプレート使用）

   ```bash
   gh pr create --base feature/issue-[N]-[epic-name] \
                --head feature/issue-[N]-[epic-name]-story[X] \
                --template .github/PULL_REQUEST_TEMPLATE/story.md
   ```

   **注意事項：**
   - `--template` と `--body` は同時に使用不可
   - `--fill-first` や `--fill` は使用禁止
   - テンプレートの全項目を埋める

5. **PR URL の確認**
   - PR が正しく作成されたことを確認
   - URL をユーザーに報告

## エラー対応

### テスト失敗時

1. エラーログを確認
2. 原因を特定して修正
3. 再度テスト実行
4. 失敗が続く場合は AskUserQuestion で相談

### CI チェック失敗時

1. `./scripts/ci-check-local.sh --yes` を実行
2. 失敗箇所を特定して修正
3. 再度コミット・プッシュ

### コンフリクト発生時

1. ベースブランチの最新を取得
2. リベースまたはマージで解決
3. テストを再実行

## チェックリスト

各 Story 完了時に以下を確認：

- [ ] 全タスクのコミットが完了
- [ ] セルフレビューが全て記録済み
- [ ] tasklist.md が最新
- [ ] overview.md が更新済み
- [ ] 全テストが通過
- [ ] PR がテンプレートで作成済み
