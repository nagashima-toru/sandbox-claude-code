# 実装計画策定コマンド

## 概要

`.epic/` ディレクトリに実装計画ドキュメントを作成します。

**対応ステップ**: 7. 実装計画策定（.epic/ 作成）

---

## 使用方法

```bash
# Issue番号を指定して実行
/plan-epic 88
```

---

## 前提条件

- Issue に `spec-approved` ラベルが付与されている
- Issue がオープン状態である
- 仕様PR（OpenAPI + 受け入れ条件）がマージ済みである

---

## 実行フロー

### 1. Issue情報の取得と前提条件チェック

```bash
gh issue view [Issue番号] --json title,labels,state,body
```

**確認項目**:
- Issue に `spec-approved` ラベルがあるか
- Issue がオープン状態か

**エラー時**: 上記条件を満たさない場合はエラーメッセージを表示：
```
❌ Issue #88 に spec-approved ラベルが付与されていません

仕様PRをマージした後、以下のコマンドでラベルを付与してください：
/update-spec-approved 88 [PR番号]
```

### 2. 仕様情報の収集

#### 2.1 Issue本文から仕様PRとファイルパスを抽出

Issue本文（または最新のコメント）から以下を取得：
- 仕様PR番号
- OpenAPI仕様ファイルのパス
- 受け入れ条件ファイルのパス

#### 2.2 仕様ファイルを読み込む

```bash
# OpenAPI仕様
cat specs/openapi/openapi.yaml

# 受け入れ条件
cat specs/acceptance/[機能名]/*.feature
```

### 3. Epicディレクトリの作成

**ディレクトリ名の形式**: `YYYYMMDD-issue-N-[title]`

```bash
mkdir -p .epic/[YYYYMMDD]-[issue-N]-[title]/
```

**例**:
```bash
mkdir -p .epic/20260207-88-auth/
```

**title の決定**:
- Issue タイトルから英数字とハイフンのみ抽出
- 小文字化（例: "認証・認可機能" → "auth"）

### 4. requirements.md の生成

**目的**: 何を実装するかを明確にする

**内容**:
```markdown
# [Epicタイトル] 要求仕様

Issue: #[N]

## 1. 概要

### 1.1 目的

[Issue本文から目的を抽出]

### 1.2 スコープ

[Issue本文からPhase定義を抽出]

### 1.3 前提条件

[Issue本文から前提条件を抽出]

---

## 2. 機能要求

[OpenAPI仕様から機能要求を抽出]

### 2.1 [機能名]

[エンドポイント定義から詳細を記述]

---

## 3. 非機能要求

### 3.1 セキュリティ

[セキュリティ要件を記述]

### 3.2 パフォーマンス

[パフォーマンス要件を記述]

### 3.3 可用性

[可用性要件を記述]

### 3.4 拡張性

[将来の拡張性を記述]

---

## 4. エラーハンドリング

[エラーハンドリング仕様を記述]

---

## 5. 受け入れ基準

[受け入れ条件ファイルから抽出、チェックリスト形式]

---

## 6. 参考資料

[関連ドキュメントへのリンク]
```

**ファイル出力**:
```bash
# 生成したMarkdownを保存
cat > .epic/[YYYYMMDD]-[issue-N]-[title]/requirements.md << 'EOF'
[生成した内容]
EOF
```

### 5. design.md の生成

**目的**: どう実装するかを明確にする

**内容**:
```markdown
# [Epicタイトル] 技術設計

Issue: #[N]

## 1. 技術選定

### 1.1 バックエンド

| 項目 | 選定 | 理由 |
|------|------|------|
| [技術項目] | [選定した技術] | [理由] |

### 1.2 フロントエンド

| 項目 | 選定 | 理由 |
|------|------|------|
| [技術項目] | [選定した技術] | [理由] |

---

## 2. アーキテクチャ設計

### 2.1 パッケージ構成

[パッケージ構成を記述]

### 2.2 フロー図

[認証フローなどの図を記述]

---

## 3. データベース設計

### 3.1 テーブル定義

[テーブル定義を記述]

### 3.2 マイグレーション戦略

[Flyway マイグレーションファイルの方針]

---

## 4. 設定ファイル

### 4.1 application.yml

[必要な設定項目を列挙]

### 4.2 環境変数

[環境変数の定義]

---

## 5. OpenAPI仕様変更

[仕様PRで追加したエンドポイントの詳細]

---

## 6. フロントエンド設計

### 6.1 コンポーネント設計

[必要なコンポーネントを列挙]

### 6.2 状態管理

[状態管理の方針]

---

## 7. テスト戦略

### 7.1 単体テスト

[単体テストの方針]

### 7.2 統合テスト

[統合テストの方針]

### 7.3 E2Eテスト

[E2Eテストの方針（オプション）]

---

## 8. セキュリティ考慮事項

[OWASP Top 10などのセキュリティ考慮事項]

---

## 9. パフォーマンス考慮事項

[パフォーマンスチューニングの方針]
```

**ファイル出力**:
```bash
cat > .epic/[YYYYMMDD]-[issue-N]-[title]/design.md << 'EOF'
[生成した内容]
EOF
```

### 6. Story分割

**原則**:
- 各Storyは独立して実装・テスト可能
- 1 Story = 2-4時間程度
- 依存関係を明確にする

**分割例（認証・認可機能）**:
1. Story 1: ユーザー管理基盤（User エンティティ、リポジトリ、DB）
2. Story 2: JWT認証基盤（トークン生成・検証）
3. Story 3: 認証エンドポイント（ログイン・リフレッシュ・ログアウト）
4. Story 4: 認可機能（ロールベースアクセス制御）
5. Story 5: OpenAPI仕様の更新
6. Story 6: フロントエンド認証対応
7. Story 7: 結合テストと最終確認

### 7. 各Story の tasklist.md 生成

**ディレクトリ作成**:
```bash
mkdir -p .epic/[YYYYMMDD]-[issue-N]-[title]/story[N]-[name]/
```

**tasklist.md の内容**:
```markdown
# Story [N]: [Story名]

## Story 概要

**目的**: [Storyの目的]

**受け入れ基準**:
- [ ] [受け入れ条件1]
- [ ] [受け入れ条件2]

---

## タスクリスト

### Task [N].1: [タスク名]

**見積もり**: [時間]

**作業内容**:
- [作業内容の詳細]

**完了条件**:
- [ ] [完了条件1]
- [ ] [完了条件2]

---

### Task [N].2: [タスク名]

...

---

## 進捗

- 開始日時:
- 完了日時:
- 実績時間:
- メモ:
```

**タスク粒度**:
- 1タスク = 1時間以内
- 具体的で検証可能
- 依存関係を考慮

### 8. overview.md の生成

**目的**: Epic全体の管理（エントリーポイント）

**内容**:
```markdown
# [Epicタイトル] 実装計画

Issue: #[N]

## Epic 概要

**目的**: [Epicの目的]

**完了条件**:
- [完了条件1]
- [完了条件2]

## ドキュメント

| ファイル | 内容 |
|---------|------|
| [requirements.md](./requirements.md) | 機能要求、非機能要求、受け入れ基準 |
| [design.md](./design.md) | 技術選定、アーキテクチャ、データベース設計 |
| このファイル (overview.md) | Epic 全体の概要、Story 構成、進捗管理 |

---

## Story 構成

| Story | 内容 | Task数 | 見積もり | ディレクトリ |
|-------|------|--------|---------|------------|
| **Story 1** | [Story名] | [N] | [時間] | [story1-xxx](./story1-xxx/) |
| **Story 2** | [Story名] | [N] | [時間] | [story2-xxx](./story2-xxx/) |
| **合計** | | **[N]** | **約[N]時間** | |

---

## 依存関係

```
[依存関係図]
```

**推奨実装順序**:
1. Story 1 → Story 2 → ...

---

## PR 作成ガイド

### Story ブランチ → Epic ベースブランチの PR

```bash
gh pr create --base feature/issue-[N]-[name] \
             --head feature/issue-[N]-[name]-story[X] \
             --template .github/PULL_REQUEST_TEMPLATE/story.md
```

### Epic ベースブランチ → master の最終 PR

```bash
gh pr create --base master \
             --head feature/issue-[N]-[name] \
             --template .github/PULL_REQUEST_TEMPLATE/epic.md
```

---

## ブランチ戦略

```
master
  └── feature/issue-[N]-[name]
       ├── feature/issue-[N]-[name]-story1
       ├── feature/issue-[N]-[name]-story2
       └── ...
```

詳細は [CLAUDE.md](../../CLAUDE.md) を参照。

---

## 進捗管理

各 Story の詳細なタスクリストは、対応するディレクトリの `tasklist.md` を参照。

### Story 1: [Story名]
- [ ] Task 1.1: [タスク名]
- [ ] Task 1.2: [タスク名]
...

### Story 2: [Story名]
- [ ] Task 2.1: [タスク名]
- [ ] Task 2.2: [タスク名]
...
```

**ファイル出力**:
```bash
cat > .epic/[YYYYMMDD]-[issue-N]-[title]/overview.md << 'EOF'
[生成した内容]
EOF
```

### 9. 結果の表示

生成結果とNext Stepsをユーザーに表示：

```
✅ 実装計画を策定しました

Epic ディレクトリ: .epic/20260207-88-auth/

## 生成したファイル

- requirements.md - 機能要求、受け入れ基準
- design.md - 技術設計、アーキテクチャ
- overview.md - Epic全体の概要（エントリーポイント）
- story1-user-management/tasklist.md
- story2-jwt-auth/tasklist.md
- story3-auth-endpoints/tasklist.md
- story4-authorization/tasklist.md
- story5-openapi/tasklist.md
- story6-frontend/tasklist.md
- story7-integration/tasklist.md

## Story 構成（全7 Story、26タスク、約16.5時間）

1. Story 1: ユーザー管理基盤（4タスク、2時間）
2. Story 2: JWT認証基盤（5タスク、3.25時間）
3. Story 3: 認証エンドポイント（4タスク、3.25時間）
4. Story 4: 認可機能（3タスク、1.75時間）
5. Story 5: OpenAPI仕様更新（2タスク、1時間）
6. Story 6: フロントエンド対応（6タスク、3.75時間）
7. Story 7: 結合テスト（2タスク、1.5時間）

## Next Steps

1. **計画レビュー**（ステップ8）
   ```bash
   /review-plan 88
   ```
   - Story分割の妥当性を確認
   - 依存関係をチェック

2. **Epic ベースブランチ作成**（ステップ9）
   ```bash
   git checkout master
   git pull origin master
   git checkout -b feature/issue-88-auth
   git push origin feature/issue-88-auth
   ```

3. **実装開始**（ステップ9）
   ```bash
   /implement-epic 88
   ```
   または個別Storyの実装：
   ```bash
   /implement-story 88 1
   ```
```

---

## エラーハンドリング

### spec-approved ラベルがない
```
❌ Issue #88 に spec-approved ラベルが付与されていません

仕様PRをマージした後、以下のコマンドを実行してください：
/update-spec-approved 88 [PR番号]
```

### ディレクトリがすでに存在
```
❌ ディレクトリ .epic/20260207-88-auth は既に存在します

既存の計画を削除するか、別のディレクトリ名を使用してください
```

### 仕様ファイルが見つからない
```
❌ OpenAPI仕様ファイルが見つかりません

Issue #88 のコメントに仕様ファイルのパスが記載されているか確認してください
```

---

## 注意事項

- `.epic/` ディレクトリは `.gitignore` に含まれており、Git管理対象外です
- 実装計画は各開発者のローカルで管理されます
- Story分割は要件に応じて柔軟に調整してください
- タスク粒度は1時間以内を目安にしてください

---

## 参考資料

- [Epic Documents](../../docs/EPIC_DOCUMENTS.md) - Epic構造の詳細
- [CLAUDE.md](../../CLAUDE.md) - 開発プロセス全体
- 既存Epic例: `.epic/20260203-88-auth/` - 参考実装

---

## 関連コマンド

- `/update-spec-approved [Issue番号] [PR番号]` - Issue更新（前のステップ）
- `/review-plan [Issue番号]` - 計画レビュー（次のステップ）
- `/implement-epic [Issue番号]` - Epic実装開始
- `/epic-status [Issue番号]` - Epic進捗確認
