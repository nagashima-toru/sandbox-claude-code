---
name: implement-epic
description: Execute Story implementation workflow including task management, testing, and PR creation for Epic realization.
---

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
   - Epic の完了状態（全 Story に ✅ マークがあるか）
3. AskUserQuestion で選択肢を表示：

   ```
   実装する Epic を選択してください

   選択肢:
   - Epic #88: 認証・認可機能 (Story 7/7 完了) ✅ 完了済み
   - Epic #92: 新機能XYZ (Story 0/5 完了)
   - Epic #112: data-testid 導入 (Story 2/3 完了)
   ```

   **表示形式**:
   - 未完了 Epic: `Epic #[N]: [タイトル] (Story [完了数]/[総数] 完了)`
   - 完了済み Epic: `Epic #[N]: [タイトル] (Story [総数]/[総数] 完了) ✅ 完了済み`

4. ユーザーが選択した Epic で処理を開始
5. **完了済み Epic を選択した場合**: 「実行前の必須確認」セクションの完了済み Epic 処理フローに従う

## 実行前の必須確認

1. **Epic 完了状態の確認**（最優先）

   Epic が既に完了している可能性があるため、まず完了状態を確認：

   ```bash
   # overview.md の全 Story に ✅ マークがあるか確認
   grep "^### Story" .epic/[日付]-[issue番号]-[epic名]/overview.md
   ```

   **完了済み Epic の判定基準**:
   - 全 Story に ✅ マークが付いている
   - Epic ベースブランチが master にマージ済み（`git log --oneline --grep="#[Issue番号]" master` で確認）
   - 全 Story の PR がマージ済み

   **完了済み Epic を検出した場合の処理**:

   a. ユーザーに報告：

   ```
   Epic #[N] は既に完了しています：
   - 全 Story ([M]/[M]) 完了
   - Epic ベースブランチ: master にマージ済み（または存在しない）
   - 実装済み日時: [日付]
   ```

   b. 次のアクションを提案（AskUserQuestion）：
   - **Epic 全体の振り返りを実施** (`/retrospective Epic #[N]`) - 推奨
   - **Epic 詳細状況を確認** (`/epic-status [N]`)
   - **別の未完了 Epic を実装**
   - **何もしない**

   c. ユーザーの選択に応じて処理：
   - 振り返り選択時: `/retrospective` スキルを実行
   - 別 Epic 選択時: Epic 選択からやり直し
   - 何もしない: スキル終了

   **重要**: 完了済み Epic に対して実装を進めない。無駄な作業を避ける。

2. **Epic 状況の確認**（未完了の場合）
   - `.epic/[日付]-[issue番号]-[epic名]/overview.md` を読む
   - 未完了の Story を特定する
   - 次に実装すべき Story を確認する

3. **現在のブランチ確認**

   ```bash
   git branch --show-current
   git status
   ```

   - 現在 Epic ベースブランチ（例: `feature/issue-88-auth`）にいることを確認
   - Working tree が clean であることを確認

4. **作業ディレクトリ確認**（重要）

   ```bash
   pwd
   ```

   - **必ずプロジェクトルート** (`/Users/.../sandbox-claude-code`) にいることを確認
   - ルート以外にいる場合は `cd` でルートに戻る
   - **理由**: git コマンド、スクリプト実行は基本的にルートから実行する必要がある

   **よくある問題**:
   - `cd frontend` で移動したまま git コマンドを実行 → パスエラー
   - `frontend/frontend` のようなネストしたディレクトリを認識してしまう

   **ベストプラクティス**:
   - frontend での作業時: `cd frontend && pnpm [command] && cd ..`
   - 常に `pwd` で位置を確認してから git コマンドを実行

## 実装フロー

### Phase 1: Story の開始

1. **前提条件の確認**（Story 2 以降は必須）

   **1.1. 前の Story の完了確認**

   ```bash
   # overview.md で前の Story が ✅ マークされているか確認
   grep -A 10 "Story [N-1]" .epic/[日付]-[issue番号]-[epic名]/overview.md
   ```

   - 前の Story が未完了（✅ なし）の場合は実装を中断
   - AskUserQuestion で「Story [N-1] が未完了ですが、Story [N] を開始しますか？」と確認

   **1.2. 前の Story の PR マージ確認**

   ```bash
   # 前の Story の PR がマージされているか確認
   gh pr list --head feature/issue-[N]-[epic-name]-story[N-1] --state merged
   ```

   - マージされていない場合は警告を表示
   - AskUserQuestion で「Story [N-1] の PR がマージされていませんが、Story [N] を開始しますか？」と確認
   - **推奨**: 前の Story のマージを待つ（依存関係の問題を防ぐため）

2. **Epic ベースブランチの最新化**（Story 2 以降、前 Story マージ済みの場合）

   前の Story がマージされている場合、Epic ベースブランチを最新化する：

   ```bash
   # Epic ベースブランチに切り替え
   git checkout feature/issue-[N]-[epic-name]

   # リモートの最新を取得
   git pull origin feature/issue-[N]-[epic-name]

   # 最新化されたことを確認
   git log --oneline -5
   ```

   - 前の Story のマージコミットが含まれていることを確認
   - 含まれていない場合は `git pull` を再実行
   - コンフリクトがある場合は解決してから次に進む

   **重要**: この手順を省略すると、前の Story の変更が含まれない状態で Story ブランチを作成してしまい、テスト失敗や手戻りの原因となる

3. **Story ディレクトリの確認**
   - `.epic/[日付]-[issue番号]-[epic名]/story[N]-[name]/tasklist.md` を読む
   - 全タスクの内容と受け入れ条件を理解する

4. **Story ブランチの作成**

   ```bash
   git checkout -b feature/issue-[N]-[epic-name]-story[X]
   ```

5. **TaskCreate でタスク管理開始**
   - tasklist.md の各タスクを TaskCreate で登録
   - 見積もり時間も記録

### Phase 2: タスクの実装

**各タスクごとに以下を実行：**

1. **タスク開始**

   ```
   TaskUpdate taskId=[id] status=in_progress
   ```

2. **実装**

   **作業ディレクトリの管理**（重要）:

   - **常にプロジェクトルートディレクトリで作業を開始する**
   - frontend または backend での作業が必要な場合のみ `cd` で移動
   - 作業完了後は必ず `cd ..` でルートに戻る
   - git コマンドは基本的にルートディレクトリから実行

   ```bash
   # ❌ 悪い例: frontend ディレクトリで git commit
   cd frontend
   git add src/...  # パスが間違う

   # ✅ 良い例: ルートディレクトリから実行
   cd frontend && pnpm format && cd ..
   git add frontend/src/...
   ```

   **実装前の事前確認**:

   - 新しいタイプのコンポーネント（Context, Provider, カスタム Hook など）を実装する前に、既存の類似実装パターンを確認
   - テストを書く前に、同じタイプのテストファイルを Read して、プロジェクトのテストパターンに従う
   - Storybook ストーリーを作成する前に、既存のストーリーファイルを Read して、型定義や構造を確認

   **例**: Context を実装する場合

   ```bash
   # 1. 既存の Context パターンを確認
   Read frontend/src/contexts/AuthContext.tsx

   # 2. 既存のテストパターンを確認
   Read frontend/tests/unit/hooks/useAuthContext.test.tsx

   # 3. パターンに従って実装
   ```

   **基本ガイドライン**:

   - CLAUDE.md の開発ガイドラインに従う
   - Backend: Clean Architecture、JUnit テスト必須
   - Frontend: Functional components、named exports、テスト・Storybook 必須

   **テストケース変更時の特別ルール**:

   既存のテストケースを変更する場合（アサーション、期待値、テストロジックなど）：

   a. **変更理由の明確化**
      - なぜテストケースを変更する必要があるのか
      - 仕様変更か、テストの不具合修正か、リファクタリングか

   b. **ユーザーへの確認**（必須）

      AskUserQuestion で以下を確認：

      ```
      「既存のテストケース [ファイル名:行番号] を変更する必要があります。

      変更内容:
      - Before: [現在のテストコード]
      - After: [変更後のテストコード]

      変更理由: [理由]

      この変更を行ってよろしいですか？」
      ```

      - テストケースの変更は仕様の変更を意味する可能性がある
      - 自己判断での変更は避け、必ずユーザーの承認を得る

   c. **新規テスト追加の優先**
      - 既存テストを変更するより、新規テストを追加する方が安全
      - 既存テストは残したまま、新しいテストケースを追加できないか検討

3. **ローカルテスト**（テスト実行後は必ずルートに戻る）

   **重要**: テスト実行後は必ず `cd ..` でプロジェクトルートに戻る

   ```bash
   # Backend の場合
   cd backend && ./mvnw test && cd ..

   # Frontend の場合
   cd frontend && pnpm test && cd ..
   ```

   - 全テストが通ることを確認
   - テスト実行後、次の git コマンドのために必ずルートディレクトリに戻る

   **統合テスト失敗時のトラブルシューティング**:

   統合テストが失敗した場合、以下をチェック：

   | 症状 | 原因の可能性 | 確認方法 | 解決策 |
   |------|-------------|---------|--------|
   | ログイン認証が失敗 | 古いパスワードハッシュが残っている | ログを確認: "Failed login attempt" | `ON CONFLICT DO UPDATE` を使用 |
   | ランダムに認証失敗 | パスワードハッシュを使い回している | setUp() のコードを確認 | 各ユーザーで個別に `passwordEncoder.encode()` を呼び出す |
   | 前回のテストデータが残る | トランザクションロールバックが無効 | `@Transactional` の有無を確認 | テストクラスに `@Transactional` を追加 |
   | ポート競合エラー | `integration-test` ゴールを使用 | コマンド履歴を確認 | `./mvnw verify` を使用 |

   詳細は `backend/CLAUDE.md` の「Integration Test Best Practices」セクションを参照。

4. **セルフレビューの実施と記録**
   - 実装内容を確認
   - 重要な指摘事項を抽出
   - `.epic/[日付]-[issue番号]-[epic名]/story[N]-[name]/self-review-task[N].[M].md` に記録
   - 必要に応じて修正を実施

5. **Storybook の型チェック**（Frontend のみ、Storybook ストーリーを作成した場合）

   Storybook ストーリーを作成した場合、コミット前に必ず型チェックを実行：

   ```bash
   cd frontend && pnpm type-check && cd ..
   ```

   - Storybook の Story 型（`args`, `render` など）は TypeScript で厳密にチェックされる
   - 型エラーがある場合は pre-commit hook で失敗するため、事前確認が重要
   - 型エラーを早期発見することで、手戻りを防ぐ

   **よくある型エラー**:
   - `args` プロパティが必要なのに `render` だけを指定している
   - Context の型が `| undefined` を含んでいない
   - Story の `render` 関数の引数型が合っていない

6. **tasklist.md の更新**
   - 完了条件のチェックボックスを更新
   - 実績時間とメモを記録

7. **タスク完了**

   ```
   TaskUpdate taskId=[id] status=completed
   ```

8. **コミット**

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

2. **Story 全体のテスト実行**（必須）

   Story の全タスク完了後、push 前に必ずテストを実行する：

   **重要**: テスト実行後は必ず `cd ..` でプロジェクトルートに戻る

   ```bash
   # Backend の場合
   cd backend && ./mvnw test && cd ..

   # Frontend の場合
   cd frontend && pnpm test && cd ..

   # E2E テストを修正した場合
   cd frontend && pnpm test:e2e && cd ..
   ```

   - テスト実行後、次の git コマンドのために必ずルートディレクトリに戻る
   - `pwd` で現在位置を確認してから次の操作に進む

   **重要**: テストコードを修正した場合、必ず以下を実行：
   - [ ] 修正したテストを実行して成功することを確認
   - [ ] 全テストを実行してリグレッションがないことを確認
   - [ ] テスト結果を tasklist.md に記録
   - [ ] **テスト実行後、プロジェクトルートに戻ったことを確認**（`pwd`）

   **テスト未実行での push は禁止**:
   - テストを実行せずに push すると CI で失敗し、手戻りが発生する
   - 「e2e テストの確認したよな？」と言われないように、必ずローカルで確認する

3. **CI チェックのローカル実行**（推奨）

   push 前に CI チェックをローカルで実行することを推奨：

   ```bash
   ./scripts/ci-check-local.sh
   ```

   - 時間がかかる場合はスキップ可能だが、CI 失敗のリスクがある

4. **最終確認**（Story 完了チェックリスト）

   **必須項目**:
   - [ ] **作業ディレクトリがプロジェクトルートであることを確認**（`pwd`）
   - [ ] 全タスクが完了している（TaskList で確認）
   - [ ] 全テストが通過している
     - [ ] 単体テスト成功（`./mvnw test` または `pnpm test`）
     - [ ] 統合テスト成功（該当する場合）
     - [ ] E2Eテスト成功（該当する場合）
   - [ ] `./mvnw verify` または `pnpm build` が成功している
   - [ ] セルフレビューが全て記録されている
   - [ ] tasklist.md の進捗が更新されている
   - [ ] overview.md に ✅ マークを追加している
   - [ ] **既存のテストが全て通過することを確認**（影響を受けるテストを修正した場合）

   **推奨項目**:
   - [ ] CI チェックをローカルで実行（`./scripts/ci-check-local.sh`）
   - [ ] コミットメッセージが適切（Co-Authored-By 含む）
   - [ ] 変更ファイルが適切にステージングされている

5. **ブランチのプッシュ**

   ```bash
   git push origin feature/issue-[N]-[epic-name]-story[X]
   ```

6. **PR 作成**（必須: テンプレート使用）

   **CRITICAL**: 必ず `--template` オプションを使用すること。テンプレートを使わないと Implementation Check が失敗します。

   ```bash
   gh pr create --base feature/issue-[N]-[epic-name] \
                --head feature/issue-[N]-[epic-name]-story[X] \
                --template .github/PULL_REQUEST_TEMPLATE/story.md
   ```

   **テンプレート (`story.md`) で埋めるべき項目**:
   - Story: #[Issue番号]
   - Story 概要: Story [X]: [Story名]
   - 変更内容（実装した内容を箇条書き）
   - このPRで検証した受け入れ条件（tasklist.md のタスクリストをコピー）
   - テスト（単体テスト、統合テスト、ローカルで動作確認のチェックボックス）
   - 備考（補足事項があれば）

   **重要な注意事項**:
   - `--template` オプションは**必須**（Implementation Check のため）
   - `--body` や `--body-file` は使用しない
   - テンプレートは GitHub が自動的に表示するので、PR 作成後にブラウザで編集する
   - CI チェックが通るまで待ってからレビュー依頼する

7. **PR URL の確認**
   - PR が正しく作成されたことを確認
   - URL をユーザーに報告

8. **Story の振り返り**（推奨）

   Story 実装完了後、学びや改善点を記録：

   ```bash
   /retrospective Story[X]: [Story名]
   ```

   **記録内容**:
   - ✅ うまくいったこと
   - 📚 学んだこと
   - ⚠️ 改善点・課題
   - 🤖 自動化・スキル提案（CLAUDE.md、スクリプト、新規スキル）
   - 📋 次のアクション

   **保存場所**: `.retrospectives/[epic-dir]/story[N]-[name]-[timestamp].md`
   - Epic #88 の Story1 の場合: `.retrospectives/20260203-issue-88-auth/story1-user-registration-20260208-150000.md`
   - 同じ Epic の振り返りが1つのフォルダにまとまり、後で Epic 全体の振り返りを行う際に参照できる

   **スキップ条件**:
   - 非常にシンプルな Story（1タスク、30分未満）
   - 時間的制約がある場合

   **重要**: 自動化・スキル提案は必ず記録。繰り返し作業が発生した場合は、スクリプト化やスキル化を検討する。

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
