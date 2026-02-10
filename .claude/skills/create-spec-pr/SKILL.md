---
name: create-spec-pr
description: Understand requirements, investigate current implementation, and create OpenAPI specification and acceptance criteria.
---

# 仕様 PR 作成コマンド

## 概要

要求仕様を理解し、現在の実装を調査した上で、OpenAPI仕様と受け入れ条件を作成し、仕様PRを作成します。

**対応ステップ**: 2. 要求仕様の理解 + 3. 現在の実装調査 + 4. 仕様 PR 作成

---

## 使用方法

```bash
# Issue番号を指定して実行
/create-spec-pr 88
```

---

## 前提条件

- Epic Issue が作成済み（`epic` ラベルが付与されている）
- Issue がオープン状態である

---

## 実行フロー

### Step 1: Issue情報の取得と前提条件チェック

```bash
gh issue view [Issue番号] --json title,labels,state,body
```

**確認項目**:

- Issue に `epic` ラベルがあるか
- Issue がオープン状態か

**エラー時**: 上記条件を満たさない場合はエラーメッセージを表示して終了

---

### Step 2: 要求仕様の理解

#### 2.1 Issue本文の詳細分析

Issue本文から以下を抽出・理解：

**必須項目**:

- **背景**: なぜこの機能が必要か
- **目的**: 何を達成するか
- **スコープ**: Phase定義、含むもの/含まないもの
- **機能要求**: 実装すべき機能の詳細
- **非機能要求**: セキュリティ、パフォーマンス、可用性

**分析観点**:

```markdown
1. ユーザーストーリー
   - 誰が（Who）
   - 何を（What）
   - なぜ（Why）

2. ビジネス要求
   - 解決する課題
   - 期待される効果
   - 成功指標

3. 技術要求
   - 必要なAPI
   - データモデル
   - 外部連携
```

#### 2.2 要求の構造化

理解した要求を以下の構造で整理：

```markdown
## 機能要求サマリー

### [機能1]: [機能名]
- **目的**: [目的]
- **エンドポイント**: [HTTPメソッド] [パス]
- **入力**: [リクエストパラメータ]
- **出力**: [レスポンス]
- **制約**: [バリデーション、認証、認可]

### [機能2]: [機能名]
...
```

#### 2.3 不明点の確認

要求が不明確な場合、AskUserQuestion で確認：

- 認証方式（JWT、OAuth、Basic Auth）
- エラーハンドリング方針
- データモデルの詳細
- 非機能要求（レスポンスタイム、スループット）

---

### Step 3: 現在の実装調査

#### 3.1 既存のAPI構造を調査

**OpenAPI仕様の確認**:

```bash
# 既存のエンドポイントを確認
cat specs/openapi/openapi.yaml
```

**確認項目**:

- 既存のエンドポイント構造（パスの命名規則）
- 既存のスキーマ定義（再利用可能なスキーマ）
- 認証・認可の設定（security schemes）
- エラーレスポンス形式（RFC 7807準拠か）

#### 3.2 関連する既存コードを調査

**バックエンドの調査**:

```bash
# Serenaツールで関連クラスを検索
mcp__serena__find_symbol [関連シンボル名]
mcp__serena__search_for_pattern [関連パターン]

# 例: 認証機能の場合
# - Security設定クラス
# - 既存のController
# - 既存のエンティティ
# - 既存のリポジトリ
```

**データベーススキーマの確認**:

```bash
# Flywayマイグレーションファイルを確認
ls backend/src/main/resources/db/migration/
cat backend/src/main/resources/db/migration/V*.sql

# 既存テーブル構造を理解
# - どのテーブルが存在するか
# - 関連するテーブルは何か
# - 追加が必要なカラムはあるか
```

**フロントエンドの確認**:

```bash
# 既存のAPI呼び出しパターンを確認
cat frontend/src/api/*

# 既存のコンポーネント構造を確認
ls frontend/src/components/
```

#### 3.3 影響範囲の特定

**調査結果のまとめ**:

```markdown
## 影響範囲分析

### 変更が必要な箇所
- [ ] OpenAPI仕様（新規エンドポイント追加）
- [ ] バックエンド（Controller、Service、Repository）
- [ ] データベース（新規テーブルまたはカラム追加）
- [ ] フロントエンド（API呼び出し、UI）

### 既存機能への影響
- [ ] 既存API: [影響あり/なし]
- [ ] 既存データモデル: [変更あり/なし]
- [ ] Breaking Changes: [あり/なし]

### 依存関係
- 依存する既存機能: [リスト]
- 影響を受ける既存機能: [リスト]
```

#### 3.4 技術選定

調査結果を元に技術を選定：

```markdown
## 技術選定

### 認証・認可方式
- [ ] JWT（推奨）
- [ ] OAuth 2.0
- [ ] Basic Auth

### データベース変更
- [ ] 新規テーブル作成
- [ ] 既存テーブル拡張
- [ ] マイグレーション戦略

### ライブラリ・フレームワーク
- Spring Security（バックエンド認証）
- jjwt（JWT処理）
- その他...
```

---

### Step 4: 仕様ブランチの作成

### Step 4: 仕様ブランチの作成

```bash
git checkout master
git pull origin master
git checkout -b spec/issue-[N]-[name]
```

**ブランチ名の形式**:

- `spec/issue-88-auth`（Issue番号 + 短い識別子）
- Issue タイトルから識別子を自動生成（スペース→ハイフン、小文字化）

---

### Step 5: OpenAPI仕様の作成

#### 5.1 調査結果を元にエンドポイントを設計

**Step 2（要求理解）とStep 3（実装調査）の結果を統合**：

- 要求仕様から必要なエンドポイントを特定
- 既存のパス命名規則に従う
- 既存のスキーマを可能な限り再利用

#### 5.2 OpenAPI仕様に追加

`specs/openapi/openapi.yaml` に以下を追加：

**paths セクション**:

```yaml
  /api/[resource]/[action]:
    [method]:
      tags:
        - [Tag]
      summary: [サマリー]
      security: []  # 認証不要の場合
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/[RequestSchema]'
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/[ResponseSchema]'
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          $ref: '#/components/responses/Unauthorized'
```

**components/schemas セクション**:

```yaml
    [RequestSchema]:
      type: object
      required:
        - [field1]
      properties:
        [field1]:
          type: string
          minLength: 1
          maxLength: 50

    [ResponseSchema]:
      type: object
      required:
        - [field1]
      properties:
        [field1]:
          type: string
```

---

### Step 6: 受け入れ条件の作成

#### 6.1 ディレクトリを作成

```bash
mkdir -p specs/acceptance/[機能名]
```

**機能名の決定**:

- Issue タイトルから抽出（例: `auth`, `user-management`）
- 英数字とハイフンのみ使用

#### 6.2 要求仕様から受け入れ条件を抽出

**Step 2（要求理解）から受け入れ基準を特定**：

- 各機能の成功条件
- 異常系の挙動
- 境界値条件

#### 6.3 .feature ファイルを作成

各エンドポイントまたは機能単位で .feature ファイルを作成：

`specs/acceptance/[機能名]/[サブ機能名].feature`

**Gherkin形式（調査結果を反映）**:

```gherkin
# language: ja
@[tag1] @[tag2] @api
Feature: [機能名]

  [機能の説明]

  Background:
    Given 以下の前提条件:
      # 必要なデータのセットアップ

  @positive
  Scenario: 正常系シナリオ
    Given 以下の条件を準備する:
      | フィールド | 値     |
      | field1    | value1 |
    When [HTTPメソッド] [エンドポイント] を呼び出す
    Then ステータスコード [200] が返される
    And レスポンスボディに以下が含まれる:
      | フィールド | 値     |
      | field1    | value1 |

  @negative
  Scenario: 異常系シナリオ
    Given 以下の不正な条件を準備する:
      | フィールド | 値     |
      | field1    | invalid |
    When [HTTPメソッド] [エンドポイント] を呼び出す
    Then ステータスコード [400] が返される
    And エラーレスポンスが RFC 7807 形式である
```

**作成すべきシナリオ**:

- ✅ 正常系（Happy Path）
- ✅ 異常系（バリデーションエラー、認証エラーなど）
- ✅ 境界値テスト

---

### Step 7: ファイルのコミット

```bash
git add specs/openapi/openapi.yaml
git add specs/acceptance/[機能名]/*.feature
git commit -m "spec: Add OpenAPI and acceptance criteria for #[N]

Based on requirements analysis and implementation investigation:
- Add [エンドポイント] endpoints to OpenAPI spec
- Add acceptance criteria for [機能名]
- Include positive and negative test scenarios
- Align with existing API conventions

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Step 8: ブランチのプッシュ

```bash
git push origin spec/issue-[N]-[name]
```

---

### Step 9: 仕様PRの作成

```bash
gh pr create \
  --base master \
  --head spec/issue-[N]-[name] \
  --template .github/PULL_REQUEST_TEMPLATE/spec.md \
  --label spec
```

**PRテンプレートの自動入力**:
GitHub CLIのインタラクティブエディタが開くので、以下を入力：

- **Story**: `#[Issue番号]`
- **変更内容**: 追加したエンドポイントとスキーマ
- **変更ファイル**: 変更したファイルのリスト
- **シナリオ数**: 正常系/異常系の件数
- **Breaking Changes**: あり/なし

---

### Step 10: 結果の表示

要求理解、実装調査の結果、およびPR URLをユーザーに表示：

```
✅ 仕様 PR を作成しました

## 要求仕様の理解（Step 2）

**背景**: [要求の背景]
**目的**: [達成すべき目標]
**スコープ**: Phase 1 - [Phase定義]

**機能要求**:
- [機能1]: [説明]
- [機能2]: [説明]

## 実装調査結果（Step 3）

**既存の関連機能**:
- [既存機能1]
- [既存機能2]

**影響範囲**:
- OpenAPI: 新規エンドポイント追加
- バックエンド: [影響箇所]
- データベース: [テーブル追加/変更]
- フロントエンド: [影響箇所]

**Breaking Changes**: [あり/なし]

## 作成した仕様（Step 4）

PR: #102
URL: https://github.com/nagashima-toru/sandbox-claude-code/pull/102

**OpenAPI仕様**:
- POST /api/auth/login - ログイン
- POST /api/auth/refresh - トークンリフレッシュ
- POST /api/auth/logout - ログアウト

**受け入れ条件**:
- specs/acceptance/auth/login.feature (3シナリオ)
- specs/acceptance/auth/refresh-token.feature (2シナリオ)
- specs/acceptance/auth/logout.feature (2シナリオ)

## Next Steps

1. **仕様 PR レビュー**（ステップ5）
   - レビュアーを指定
   - フィードバックを反映
   - 要求理解と実装調査の妥当性も確認

2. **PR マージ後**（ステップ6）
   ```bash
   /update-spec-approved 88 102
   ```

- Issue に仕様を明記
- spec-approved ラベルを付与

3. **実装計画策定**（ステップ7）

   ```bash
   /plan-epic 88
   ```

   - .epic/ ディレクトリ作成
   - Story分割と実装計画

```

---

## エラーハンドリング

### Issue が存在しない
```

❌ Issue #88 が見つかりません

```

### epic ラベルがない
```

❌ Issue #88 には epic ラベルが付与されていません
まず /create-epic-issue コマンドでEpic Issueを作成してください

```

### ブランチがすでに存在
```

❌ ブランチ spec/issue-88-auth は既に存在します
既存のブランチを削除するか、別の名前を使用してください

```

### OpenAPI構文エラー
```

❌ OpenAPI仕様に構文エラーがあります
[エラー詳細]
修正してから再度コミットしてください

```

---

## 注意事項

- このコマンドは**Step 2（要求仕様の理解）、Step 3（現在の実装調査）、Step 4（仕様PR作成）**を一括で実行します
- 要求理解と実装調査を十分に行い、調査結果を元にOpenAPI仕様と受け入れ条件を作成します
- 不明点がある場合は、AskUserQuestion で確認してから仕様を作成します
- 仕様は実装の前に確定させることが重要です（手戻り防止）
- 仕様PRはmasterブランチへの直接PRです（Epic baseブランチではない）
- 既存のコード規約やAPI設計パターンに従って仕様を作成します

---

## 参考資料

- [Spec PR Guide](../../docs/development/SPEC_PR_GUIDE.md) - 仕様PR作成の詳細ガイド
- [OpenAPI Specification](https://swagger.io/specification/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
- [RFC 7807 - Problem Details](https://tools.ietf.org/html/rfc7807)

---

## 関連コマンド

- `/create-epic-issue` - Epic Issue作成（前のステップ）
- `/update-spec-approved [Issue番号] [PR番号]` - Issue更新（次のステップ）
- `/plan-epic [Issue番号]` - 実装計画策定（仕様承認後）
