# 仕様 PR ガイド

仕様駆動開発（SDD）における仕様 PR の作成方法を説明します。

---

## 仕様 PR とは

**目的**: API の契約（Contract）を定義し、実装の受け入れ基準を明確にする

**含めるもの**:

1. OpenAPI 仕様（API 定義）
2. 受け入れ条件（.feature ファイル）

---

## 作成タイミング

```
Epic Issue 作成
  ↓
要求仕様の理解
  ↓
現在の実装調査
  ↓
👉 仕様 PR 作成（ここ！）
  ↓
レビュー・マージ
  ↓
spec-approved
  ↓
実装計画策定
```

---

## 1. OpenAPI 仕様

**場所**: `specs/openapi/openapi.yaml`

### 含めるべき内容

- エンドポイント定義（paths）
- リクエストスキーマ（requestBody）
- レスポンススキーマ（responses）
- エラーレスポンス（RFC 7807 形式）
- 認証スキーム（security）

### 例（認証機能）

```yaml
paths:
  /api/auth/login:
    post:
      tags:
        - Auth
      summary: ログイン
      security: []  # 認証不要
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
      responses:
        '200':
          description: ログイン成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LoginResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'

components:
  schemas:
    LoginRequest:
      type: object
      required:
        - username
        - password
      properties:
        username:
          type: string
          minLength: 1
          maxLength: 50
        password:
          type: string
          minLength: 1

    LoginResponse:
      type: object
      required:
        - accessToken
        - refreshToken
        - tokenType
        - expiresIn
      properties:
        accessToken:
          type: string
        refreshToken:
          type: string
        tokenType:
          type: string
          example: Bearer
        expiresIn:
          type: integer
          format: int32
          example: 3600
```

---

## 2. 受け入れ条件

**場所**: `specs/acceptance/[機能名]/*.feature`

**形式**: Gherkin 記法

### 含めるべきシナリオ

- ✅ 正常系（Happy Path）
- ✅ 異常系（バリデーションエラー、認証エラーなど）
- ✅ 境界値テスト

### 例（ログイン機能）

```gherkin
# specs/acceptance/auth/login.feature
# language: ja
@auth @login @api
Feature: ログイン機能

  JWT ベースの認証機能

  Background:
    Given 以下のユーザーが存在する:
      | username | password | role  | enabled |
      | admin    | admin123 | ADMIN | true    |
      | viewer   | viewer123| VIEWER| true    |

  @positive
  Scenario: 正しい認証情報でログインする
    Given 以下のログイン情報を準備する:
      | username | password |
      | admin    | admin123 |
    When POST /api/auth/login を呼び出す
    Then ステータスコード 200 が返される
    And レスポンスボディに以下が含まれる:
      | フィールド    | 値     |
      | tokenType    | Bearer |
    And レスポンスボディに accessToken が含まれる
    And レスポンスボディに refreshToken が含まれる
    And accessToken が有効である

  @negative
  Scenario: 誤ったパスワードでログインを試みる
    Given 以下のログイン情報を準備する:
      | username | password |
      | admin    | wrong    |
    When POST /api/auth/login を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスが RFC 7807 形式である
    And エラーレスポンスに以下が含まれる:
      | フィールド | 値                          |
      | status    | 401                         |
      | title     | Authentication Error        |

  @negative
  Scenario: 存在しないユーザーでログインを試みる
    Given 以下のログイン情報を準備する:
      | username    | password |
      | nonexistent | test     |
    When POST /api/auth/login を呼び出す
    Then ステータスコード 401 が返される
```

---

## 作成手順

### Step 1: 仕様ブランチを作成

```bash
git checkout master
git pull
git checkout -b spec/issue-88-auth
```

### Step 2: OpenAPI 仕様を追加

`specs/openapi/openapi.yaml` にエンドポイントを追加

### Step 3: 受け入れ条件を作成

```bash
mkdir -p specs/acceptance/auth
# .feature ファイルを作成
```

### Step 4: PR 作成

```bash
gh pr create --base master \
             --head spec/issue-88-auth \
             --template .github/PULL_REQUEST_TEMPLATE/spec.md \
             --label spec

# PR タイトル例: "[Spec] 認証・認可機能の仕様定義"
```

**PR 本文の必須項目**:

- `Story: #88`（Issue 番号）
- 変更内容の概要
- レビュー観点

---

## レビュー・マージ後の作業

### 1. Issue に仕様を明記

Issue #88 に以下のコメントを追加：

```markdown
## ✅ 仕様承認完了

仕様 PR #XX がマージされ、仕様が確定しました。

### 承認された仕様

**仕様 PR**: #XX

**OpenAPI 仕様**:
- [`specs/openapi/openapi.yaml`](リンク)
  - `POST /api/auth/login` - ログイン
  - `POST /api/auth/refresh` - トークンリフレッシュ
  - `POST /api/auth/logout` - ログアウト

**受け入れ条件**:
- [`specs/acceptance/auth/login.feature`](リンク)
- [`specs/acceptance/auth/refresh-token.feature`](リンク)
- [`specs/acceptance/auth/logout.feature`](リンク)
- [`specs/acceptance/auth/authorization.feature`](リンク)

### Next Steps

- [ ] 実装計画策定（`.epic/20260203-88-auth/` 作成）
- [ ] Epic ベースブランチ作成
- [ ] Story 実装開始
```

### 2. spec-approved ラベル付与

Issue #88 に `spec-approved` ラベルを手動で付与

### 3. 実装計画策定

Issue のコメントを参照しながら `.epic/` を作成

---

## チェックリスト

仕様 PR 作成前に確認：

- [ ] すべてのエンドポイントが OpenAPI に定義されている
- [ ] すべてのエンドポイントに受け入れ条件がある
- [ ] 正常系と異常系の両方がカバーされている
- [ ] エラーレスポンスが RFC 7807 形式で定義されている
- [ ] 認証が必要なエンドポイントに security が設定されている
- [ ] Breaking Changes がある場合、明記されている

---

## 参考資料

- [OpenAPI Specification](https://swagger.io/specification/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
- [RFC 7807 - Problem Details](https://tools.ietf.org/html/rfc7807)
