---
name: plan-epic
description: "[Human-Only] Generate implementation plan documents in .epic/ directory and automatically review plan quality."
disable-model-invocation: true
---

# 実装計画策定コマンド

## ⛔ エージェント呼び出し禁止

このスキルは **ユーザーが直接実行するスキル** です。
他のスキルやエージェント（Task ツール）から呼び出してはいけません。

**禁止理由**:
- 複数フェーズにわたる長時間の作業を含む（Issue 調査・仕様読み込み・ドキュメント生成・レビュー）
- ユーザーとの対話（AskUserQuestion）を前提とする（Story 分割・技術選定の確認）
- `.epic/` ディレクトリへのファイル作成という副作用を含む
- 誤った呼び出しで不完全な実装計画が生成される可能性がある

**エージェントがこのスキルを実行しようとした場合**:
即座に停止し、ユーザーに「このスキルはユーザー専用です。`/plan-epic` を直接実行してください」と報告する。

---

## 概要

`.epic/` ディレクトリに実装計画ドキュメントを作成し、計画の妥当性を自動的にレビューします。

**対応ステップ**: 7. 実装計画策定（.epic/ 作成 + 計画セルフレビュー）

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

### ⚠️ 重要な前提

**仕様PRには実装コードを含まない**: SDD（仕様駆動開発）では、仕様PR（ステップ4）でOpenAPI仕様と受け入れ条件のみを追加し、実装コード（バックエンド・フロントエンド）は含めません。実装はステップ9以降で行います。

そのため、**仕様PRで追加されたAPIエンドポイントは未実装**であることを前提として実装計画を立てる必要があります：

- **バックエンド実装**: 仕様PRで追加されたエンドポイントのController、UseCase、DTOなどを実装
- **フロントエンド実装**: APIクライアント生成、Hooks、コンポーネント統合などを実装

既存のエンドポイントを使用する場合でも、実装の有無を必ず確認してください。

---

## 実行フロー

### 0. ベストプラクティスとテスト戦略の読み込み（必須・最初に実行）

計画策定を始める前に、テスト要件を把握するために以下を読み込む：

```bash
Read backend/docs/BEST_PRACTICES.md   # §2 テスト戦略（レイヤー別テスト対応表・カバレッジ目標）
Read frontend/docs/BEST_PRACTICES.md  # §4 テスト戦略（テスト種別対応表・カバレッジ目標）
```

**読み込み目的**:

- テスト戦略のレイヤー別対応表を把握し、Story のタスクに漏れなく反映する
- カバレッジ目標（Backend: Domain 90%+ / UseCase 85%+ / Mapper・Controller 80%+、Frontend: Utils/Hooks 90%+ / Components 80%+）を把握する
- 標準タスクパターン（§6/§7）を確認して tasklist.md の品質を高める

**この手順をスキップしてはいけない**: 読み込みなしで tasklist.md を生成すると、
レイヤー別テスト種別の誤り（例: UseCase を統合テストにする）や
カバレッジ目標の未記載、テストタスクの抜け漏れが発生しやすくなる。

---

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

#### 2.3 既存実装の系統的確認（重要）

仕様PRで追加されたAPIエンドポイントが既に実装されているか、**Clean Architecture レイヤー別**に系統的に確認します。

**Backend 調査フロー**:

**Step A: OpenAPI 仕様から対象エンドポイントを特定**

```bash
# 追加・変更されたエンドポイントのパス・メソッドをリストアップ
# 例: POST /api/users, GET /api/users/me
```

**Step B: Presentation Layer（Controller）の確認**

```bash
# 対象エンドポイントを処理する Controller を検索
find backend/src -name "*Controller.java" -type f | xargs grep -l "[エンドポイントのパス or メソッド名]"

# Controller が存在する場合はファイルを Read して実装内容を確認
```

**Step C: Application Layer（UseCase）の確認**

```bash
# Controller で使用される UseCase を検索
find backend/src -name "*UseCase.java" -type f | xargs grep -l "[関連キーワード]"
```

**Step D: Domain Layer（Model/Repository Interface）の確認**

```bash
# Domain Model と Repository Interface の存在を確認
find backend/src/main/java -path "*/domain/model/*.java" -type f
find backend/src/main/java -path "*/domain/repository/*.java" -type f
```

**Step E: Infrastructure Layer（Persistence/Security）の確認**

```bash
# Repository 実装と Mapper の存在を確認
find backend/src/main/java -path "*/infrastructure/persistence/*.java" -type f
```

**Step F: テストファイルの存在確認**

```bash
# 各クラスに対応するテストファイルが存在するか確認
find backend/src/test -name "*.java" -type f | head -20
```

**Frontend 調査フロー**:

**Step G: API クライアント生成コードの確認**

```bash
# Orval 生成コードに対象 API が含まれているか確認
ls -la frontend/src/lib/api/generated/
grep -r "[エンドポイント関連キーワード]" frontend/src/lib/api/generated/
```

**Step H: 既存コンポーネント・Hook・Context の確認**

```bash
# 関連するコンポーネントと Hook の存在を確認
find frontend/src -name "*.tsx" -o -name "*.ts" | xargs grep -l "[機能名]" 2>/dev/null
```

**Step I: テスト・Storybook ファイルの確認**

```bash
# テストと Storybook の存在を確認
find frontend/tests -name "*.test.*" | head -10
find frontend/src -name "*.stories.*" | head -10
```

**判定テーブル**:

| 確認結果 | Story への含め方 |
|---------|----------------|
| Backend 実装が存在する + Frontend 実装が存在する | Story 不要（既存機能の修正のみ） |
| Backend 実装が存在しない | **Backend 実装 Story を必ず追加**（Controller/UseCase/Mapper/テスト） |
| Frontend 実装が存在しない | **Frontend 実装 Story を追加**（APIクライアント再生成/Hook/Component/Story） |
| Backend のみ存在し Frontend がない | **Frontend 実装 Story を追加** |

**注意**: OpenAPI仕様に定義されていても、実装が存在しない場合がほとんどです。必ず確認してください。

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

### 4.5 技術的決定の ADR 自動作成（新規ステップ）

design.md 生成の前に、ADR が必要な技術的決定を特定して作成します。

**ADR 作成が必要な技術的決定の判断基準**:

- プロジェクト全体（他 Epic にも影響する）技術選定
- 既存の技術スタックに新しいライブラリを追加する場合
- アーキテクチャパターンの変更・追加
- 代替案が複数存在し、選定理由が後から参照価値のある決定

**ADR 作成フロー**:

```bash
# 既存 ADR の連番を確認
ls docs/adr/
# 例: 0001-use-openapi-first.md → 次は 0002

# ADR スタブを作成（決定ごとに1ファイル）
# ファイル名: docs/adr/000N-[decision-title-in-kebab-case].md
```

**ADR スタブのテンプレート**（既存の ADR-0001 の構造に準拠）:

```markdown
# ADR-000N: [決定タイトル]

## ステータス

**提案中** - [日付]（plan-epic で自動生成）

## コンテキスト

[なぜこの決定が必要か]

## 決定

[何を選択したか]

## 代替案

[検討した代替案と却下理由]

## 結果

[期待される効果とトレードオフ]

---

*この ADR は /plan-epic により自動生成されました。詳細はレビュー後に補完してください。*
```

**design.md との関係**:

- design.md 内「1. 技術選定」テーブルは維持（Epic 固有の軽微な決定用）
- プロジェクト全体に影響する重要な決定は ADR に移動し、design.md から参照リンクを追記する

**判断が難しい場合**: AskUserQuestion でユーザーに確認してから ADR を作成する。

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
| [技術項目] | [選定した技術] | [理由]（重要な決定は [ADR-000N](../../docs/adr/000N-xxx.md) 参照） |

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

[単体テストの方針（backend/docs/BEST_PRACTICES.md §2 に準拠）]

### 7.2 統合テスト

[統合テストの方針]

### 7.3 E2Eテスト

[E2Eテストの方針（オプション）]

---

## 8. セキュリティ考慮事項

[OWASP Top 10などのセキュリティ考慮事項]

---

## 9. 実装対象クラス一覧

### Backend 新規作成クラス

| クラス名 | パッケージ | 種別 | 対応 Story |
|---------|-----------|------|-----------|
| [クラス名] | [パッケージパス] | [Model/UseCase/Repository/Mapper/Controller/Test] | Story [N] |

### Backend 変更クラス

| クラス名 | パッケージ | 変更内容 | 対応 Story |
|---------|-----------|---------|-----------|
| [クラス名] | [パッケージパス] | [変更内容] | Story [N] |

### Frontend 新規作成ファイル

| ファイル名 | パス | 種別 | 対応 Story |
|----------|-----|------|-----------|
| [ファイル名] | [パス] | [Component/Hook/Context/Story/Test] | Story [N] |

### Frontend 変更ファイル

| ファイル名 | パス | 変更内容 | 対応 Story |
|----------|-----|---------|-----------|
| [ファイル名] | [パス] | [変更内容] | Story [N] |
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

**⚠️ バックエンド実装の考慮**:

仕様PRで追加されたAPIエンドポイントが未実装の場合（ほとんどの場合）、**Story 1としてバックエンドAPI実装を追加**します：

- **Story 1: バックエンドAPI実装**: Controller、UseCase、DTO、テストなど
- **Story 2以降**: フロントエンド実装やUI改善

バックエンドAPIが存在しないと、フロントエンドの実装・テストができないため、必ず最初に実装します。

**Story 分割の詳細ガイドライン**:

1. **完結性**: 各 Story は独立して完結可能な単位とする
   - ❌ 悪い例: Story A で機能実装、Story B でテスト実装
   - ✅ 良い例: Story A で機能実装 + テスト実装 + 動作確認

2. **テスタビリティ**: 各 Story の完了時点で全テストが通る状態にする
   - Story 内でコードを変更した場合、その Story 内でテストも修正・確認する
   - 「テストは次の Story で」という分割は避ける

3. **レビュー可能性**: 1つの Story は 1 PR で完結する
   - PR 単位で独立してレビュー・マージできること

4. **依存関係の明確化**: Story 間の依存関係を overview.md に明記

5. **e2e テスト影響の考慮**: UIテキスト・表示内容を変更する Story では e2e テスト更新を必須タスクとして含める
   - ラベル・ボタン・見出し・テーブルヘッダー等のテキストを変更する場合、`tests/e2e/` 内で文字列セレクター（`getByRole({ name })`・`getByLabel`・`getByText` 等）を使っている箇所を必ず確認・更新する
   - 根本対策として `data-testid` セレクターへの移行も検討し、ロケール変更への耐性を高める
   - e2e テスト更新は「次の Story で対応」とせず、テキスト変更と同じ Story に含める

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
- [ ] `pnpm test` と `pnpm type-check` が通過する
- [ ] （UIテキスト変更を含む場合）既存 e2e テストの文字列セレクターが更新されている

---

## テスト計画

| 変更内容 | テスト種別 | 理由 |
|---------|----------|------|
| [変更内容1] | Unit / Component / Integration / E2E | [なぜその種別か] |
| [変更内容2] | Unit / Component / Integration / E2E | [なぜその種別か] |

<!-- E2E 追加がある場合: 正当化理由・追加後の E2E テスト数（上限30件）を記載 -->
<!-- E2E 追加理由: [Q1〜Q4 の回答] -->
<!-- 追加後の E2E テスト数: [N]件 / 上限 30件 -->

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

**Backend 標準タスクパターン（新規エンドポイント実装時）**:

```
N.1  DB マイグレーション（Flyway）
N.2  Domain Model + 単体テスト（JUnit 5 Pure Unit）
N.3  Repository Interface 定義（domain 層）
N.4  MyBatis Mapper + 統合テスト（Testcontainers）
N.5  Repository Implementation + 単体テスト（Mockito）
N.6  UseCase + Mockito 単体テスト
N.7  Controller + MockMvc 統合テスト
```

**Frontend 標準タスクパターン（新規機能実装時）**:

```
N.1  API クライアント再生成（pnpm generate:api）
N.2  カスタム Hook + 単体テスト（QueryClientProvider ラップ）
N.3  Context（必要時）+ 単体テスト（| undefined 型パターン確認）
N.4  コンポーネント + Storybook + pnpm type-check
N.5  コンポーネントテスト（MSW モック）
N.6  テスト種別判定 + 実装
     - E2E 判定フロー（Q1〜Q4）を実行し、必要な場合のみ E2E テストを追加
     - 禁止パターン（フォームバリデーション・権限ボタン・レスポンシブ・i18n）は
       Unit/Component テストに変換する
     - 追加後の E2E テスト数が上限 30件以内であることを確認する
```

**Frontend UIテキスト変更パターン（ラベル・見出し・ボタン等の表示テキストを変更する Story）**:

```
N.1  翻訳リソースの追加・変更（messages/ja.json, messages/en.json 等）
N.2  コンポーネントのテキスト置き換え（翻訳キー参照への変換）
N.3  影響するコンポーネントテスト（Vitest）の修正
N.4  既存 e2e テストの文字列セレクター確認・更新  ← 必須
     - `grep -r "変更前テキスト" tests/e2e/` で影響箇所を特定
     - getByRole({ name }), getByLabel, getByText 等のテキスト指定を更新
     - 可能であれば data-testid セレクターへの移行を併施
     - pnpm test:e2e でローカル確認（バックエンド起動が必要な場合はスキップ可、CI で確認）
```

> **⚠️ 注意**: e2eテスト更新タスク（N.4）は「次の Story で対応」と分離しないこと。
> UIテキスト変更を含む Story の受け入れ基準には必ず「既存 e2e テストの文字列セレクターが更新されている」を含める。

これらのパターンは `backend/docs/BEST_PRACTICES.md` §6 および `frontend/docs/BEST_PRACTICES.md` §7 でも参照できます。

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

[依存関係図]

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

master
  └── feature/issue-[N]-[name]
       ├── feature/issue-[N]-[name]-story1
       ├── feature/issue-[N]-[name]-story2
       └── ...

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
- design.md - 技術設計、アーキテクチャ（実装対象クラス一覧含む）
- overview.md - Epic全体の概要（エントリーポイント）
- story1-user-management/tasklist.md
- story2-jwt-auth/tasklist.md
- ...

## Story 構成（全N Story、Mタスク、約Xh）

1. Story 1: [Story名]（Nタスク、Xh）
...

## Next Steps

**計画レビュー（ステップ8）は自動的に実行されます（Step 10参照）**

1. **Epic ベースブランチ作成**（レビュー後）
   git checkout master && git pull origin master
   git checkout -b feature/issue-88-auth
   git push origin feature/issue-88-auth

2. **実装開始**（ステップ9）
   /implement-epic 88
```

---

### 10. 計画のセルフレビュー（Task サブエージェント経由）

**重要**: ステップ9の結果表示後、Task ツールを使ってサブエージェントでレビューを実施します。
Skill ツールを直接呼び出さないこと（メインコンテキストにレビュー処理が蓄積するため）。

Task ツールを呼び出す際は以下のプロンプトを使用する（`[EPIC_DIR_PATH]` は Step 3 で生成した実際のパスに置換すること）:

- subagent_type: `"general-purpose"`
- prompt:

```
あなたは実装計画レビュアーです。以下の Epic ドキュメントをレビューし、品質評価レポートを出力してください。

## レビュー対象

Epic ディレクトリ: [EPIC_DIR_PATH]
（例: .epic/20260207-88-auth/）

## 実行手順

1. 以下のファイルを Read で読み込む:
   - backend/docs/BEST_PRACTICES.md
   - frontend/docs/BEST_PRACTICES.md
   - [EPIC_DIR_PATH]/requirements.md
   - [EPIC_DIR_PATH]/design.md
   - [EPIC_DIR_PATH]/overview.md
   - [EPIC_DIR_PATH]/story*/tasklist.md（全 Story 分）

2. .claude/skills/review-implementation/SKILL.md を読み込み、
   「plan レビュー観点」（構造的・完全性・Backend/Frontend BP・ADR）に従って評価する

3. 「出力形式」セクションの形式（✅ 🔴 🟡 🔵 ⬜ 🎯）でレポートを出力する
```

**レビュー結果に基づく対応**:

- 🔴 必須修正がある場合: 自動修正または `AskUserQuestion` でユーザーに確認して修正
- 🟡 推奨修正の場合: `AskUserQuestion` でユーザーに確認してから対応
- 🔵 提案のみの場合: ユーザーに提示して判断を委ねる

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

- このコマンドは**計画作成後に自動的に Task サブエージェント（`review-implementation plan` 相当）でレビュー**します
- レビュー結果に基づき、改善提案が表示されます
- 重大な問題がある場合は、自動的に修正して再生成します
- `.epic/` ディレクトリは `.gitignore` に含まれており、Git管理対象外です
- 実装計画は各開発者のローカルで管理されます
- Story分割は要件に応じて柔軟に調整してください
- タスク粒度は1時間以内を目安にしてください
- **ステップ8（計画レビュー）は人が行います**が、このコマンド内のセルフレビューで品質が担保されます

---

## 参考資料

- [CLAUDE.md](../../CLAUDE.md) - 開発プロセス全体
- [backend/docs/BEST_PRACTICES.md](../../backend/docs/BEST_PRACTICES.md) - バックエンドベストプラクティス
- [frontend/docs/BEST_PRACTICES.md](../../frontend/docs/BEST_PRACTICES.md) - フロントエンドベストプラクティス
- 既存Epic例: `.epic/20260203-88-auth/` - 参考実装

---

## 関連コマンド

- `/update-spec-approved [Issue番号] [PR番号]` - Issue更新（前のステップ）
- `/review-implementation plan` - 計画レビュー（本スキルが Task サブエージェント経由で自動実行）
- `/implement-epic [Issue番号]` - Epic実装開始（次のステップ）
- `/epic-status [Issue番号]` - Epic進捗確認
