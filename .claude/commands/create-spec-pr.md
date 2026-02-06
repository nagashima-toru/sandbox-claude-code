# 仕様 PR 作成コマンド

## 概要

OpenAPI仕様と受け入れ条件を作成し、仕様PRを作成します。

**対応ステップ**: 4. 仕様 PR 作成（OpenAPI + 受け入れ条件）

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
- ステップ2（要求仕様の理解）とステップ3（現在の実装調査）が完了している

---

## 実行フロー

### 1. Issue情報の取得

```bash
gh issue view [Issue番号] --json title,labels,state
```

**確認項目**:
- Issue に `epic` ラベルがあるか
- Issue がオープン状態か

**エラー時**: 上記条件を満たさない場合はエラーメッセージを表示して終了

### 2. 仕様ブランチの作成

```bash
git checkout master
git pull origin master
git checkout -b spec/issue-[N]-[name]
```

**ブランチ名の形式**:
- `spec/issue-88-auth`（Issue番号 + 短い識別子）
- Issue タイトルから識別子を自動生成（スペース→ハイフン、小文字化）

### 3. OpenAPI仕様テンプレートの生成

#### 3.1 既存のOpenAPI仕様を読み込む

```bash
cat specs/openapi/openapi.yaml
```

#### 3.2 Issueから必要なエンドポイントを抽出

Issue本文やユーザーとの対話から、追加すべきエンドポイントを特定

#### 3.3 テンプレートを追加

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

### 4. 受け入れ条件テンプレートの生成

#### 4.1 ディレクトリを作成

```bash
mkdir -p specs/acceptance/[機能名]
```

**機能名の決定**:
- Issue タイトルから抽出（例: `auth`, `user-management`）
- 英数字とハイフンのみ使用

#### 4.2 .feature ファイルを作成

各エンドポイントまたは機能単位で .feature ファイルを作成：

`specs/acceptance/[機能名]/[サブ機能名].feature`

**テンプレート（Gherkin形式）**:
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

### 5. ファイルのコミット

```bash
git add specs/openapi/openapi.yaml
git add specs/acceptance/[機能名]/*.feature
git commit -m "spec: Add OpenAPI and acceptance criteria for #[N]

- Add [エンドポイント] endpoints to OpenAPI spec
- Add acceptance criteria for [機能名]
- Include positive and negative test scenarios

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

### 6. ブランチのプッシュ

```bash
git push origin spec/issue-[N]-[name]
```

### 7. 仕様PRの作成

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

### 8. 結果の表示

PR URLと次のステップをユーザーに表示：

```
✅ 仕様 PR を作成しました

PR: #102
URL: https://github.com/nagashima-toru/sandbox-claude-code/pull/102

## 追加した仕様

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

- OpenAPI仕様と受け入れ条件は**仕様のテンプレート**を生成します
- ユーザーは生成されたテンプレートを元に、詳細を追加・修正する必要があります
- 仕様は実装の前に確定させることが重要です（手戻り防止）
- 仕様PRはmasterブランチへの直接PRです（Epic baseブランチではない）

---

## 参考資料

- [Spec PR Guide](../../docs/SPEC_PR_GUIDE.md) - 仕様PR作成の詳細ガイド
- [OpenAPI Specification](https://swagger.io/specification/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
- [RFC 7807 - Problem Details](https://tools.ietf.org/html/rfc7807)

---

## 関連コマンド

- `/create-epic-issue` - Epic Issue作成（前のステップ）
- `/update-spec-approved [Issue番号] [PR番号]` - Issue更新（次のステップ）
- `/plan-epic [Issue番号]` - 実装計画策定（仕様承認後）
