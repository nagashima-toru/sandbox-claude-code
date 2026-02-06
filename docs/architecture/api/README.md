# API 設計ガイドライン

## 目次

- [概要](#概要)
- [設計原則](#設計原則)
- [RESTful API 規約](#restful-api-規約)
- [リクエスト/レスポンス形式](#リクエストレスポンス形式)
- [エラーハンドリング](#エラーハンドリング)
- [バージョニング](#バージョニング)
- [セキュリティ](#セキュリティ)

## 概要

このドキュメントは、Sandbox プロジェクトにおける API 設計の標準とベストプラクティスを定義します。

### 設計アプローチ

**Specification-First（仕様ファースト）**

すべての API は OpenAPI 仕様の定義から始まります。

```
OpenAPI 仕様定義 → コード生成 → 実装 → テスト
```

## 設計原則

### 1. RESTful 設計

HTTP メソッドとステータスコードを適切に使用します。

| Method | 用途 | 冪等性 | 安全性 |
|--------|------|--------|--------|
| GET | リソース取得 | ✅ | ✅ |
| POST | リソース作成 | ❌ | ❌ |
| PUT | リソース更新（完全置換） | ✅ | ❌ |
| PATCH | リソース部分更新 | ❌ | ❌ |
| DELETE | リソース削除 | ✅ | ❌ |

### 2. 一貫性のある命名規則

#### URL パス

- **小文字とハイフン**を使用: `/api/user-profiles`
- **複数形**を使用: `/api/messages`（単数形 `/api/message` ❌）
- **階層的**に設計: `/api/users/{userId}/messages`

#### JSON プロパティ

- **キャメルケース**を使用: `createdAt`, `userId`
- **スネークケース**は使用しない: `created_at` ❌

### 3. OpenAPI Specification

すべての API は OpenAPI 3.0 で定義します。

```yaml
paths:
  /api/messages:
    post:
      summary: メッセージ作成
      description: |
        新しいメッセージを作成します。

        **受け入れ条件:** specs/acceptance/messages/create-message.feature
      operationId: createMessage
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MessageRequest'
      responses:
        '201':
          description: 作成成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MessageResponse'
```

### 4. API 契約の厳格な管理

- OpenAPI 仕様が Single Source of Truth
- 仕様から自動的にコード生成
- 契約違反は CI で自動検出

## RESTful API 規約

### リソース URL パターン

```
GET    /api/messages           # 一覧取得
GET    /api/messages/{id}      # 個別取得
POST   /api/messages           # 新規作成
PUT    /api/messages/{id}      # 全体更新
PATCH  /api/messages/{id}      # 部分更新
DELETE /api/messages/{id}      # 削除
```

### HTTP Status Codes

#### 成功レスポンス (2xx)

| Code | 用途 | 使用例 |
|------|------|--------|
| 200 OK | 成功（レスポンスボディあり） | GET, PUT の成功 |
| 201 Created | リソース作成成功 | POST の成功 |
| 204 No Content | 成功（レスポンスボディなし） | DELETE の成功 |

#### クライアントエラー (4xx)

| Code | 用途 | 使用例 |
|------|------|--------|
| 400 Bad Request | バリデーションエラー | 不正なリクエストボディ |
| 401 Unauthorized | 認証エラー | トークンなし・無効 |
| 403 Forbidden | 認可エラー | 権限不足 |
| 404 Not Found | リソース未検出 | 存在しない ID |
| 409 Conflict | 競合エラー | 重複するコード |
| 422 Unprocessable Entity | セマンティックエラー | ビジネスルール違反 |

#### サーバーエラー (5xx)

| Code | 用途 | 使用例 |
|------|------|--------|
| 500 Internal Server Error | サーバー内部エラー | 予期しない例外 |
| 503 Service Unavailable | サービス利用不可 | メンテナンス中 |

## リクエスト/レスポンス形式

### Content-Type

- **リクエスト**: `application/json`
- **レスポンス（正常）**: `application/json`
- **レスポンス（エラー）**: `application/problem+json` (RFC 7807)

### ページネーション

一覧取得 API はページネーションをサポートします。

#### リクエスト

```
GET /api/messages?page=0&size=20
```

**クエリパラメータ:**
- `page`: ページ番号（0始まり）、デフォルト: 0
- `size`: 1ページあたりの件数、デフォルト: 20、最大: 100

#### レスポンス

```json
{
  "content": [
    {
      "id": 1,
      "code": "MSG_001",
      "content": "Hello, World!",
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### ソート

```
GET /api/messages?sort=createdAt,desc&sort=id,asc
```

### フィルタリング

```
GET /api/messages?code=MSG_001
GET /api/messages?createdAfter=2024-01-01T00:00:00Z
```

### 日時形式

ISO 8601 形式を使用します。

```
2024-01-01T00:00:00Z          # UTC
2024-01-01T09:00:00+09:00     # タイムゾーン付き
```

## エラーハンドリング

詳細は [エラーハンドリング仕様](./error-handling.md) を参照してください。

### RFC 7807 Problem Details

すべてのエラーレスポンスは RFC 7807 形式に従います。

```json
{
  "type": "https://api.example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "The request body contains invalid fields.",
  "instance": "/api/messages",
  "timestamp": "2024-01-01T00:00:00Z",
  "errors": [
    {
      "field": "code",
      "rejectedValue": "",
      "message": "コードは必須です"
    }
  ]
}
```

Content-Type: `application/problem+json`

### バリデーションルール

#### 必須フィールド

```json
{
  "code": "MSG_001",    // 必須
  "content": "Hello"    // 必須
}
```

エラー例:
```json
{
  "type": "https://api.example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "The request body contains invalid fields.",
  "instance": "/api/messages",
  "timestamp": "2024-01-01T00:00:00Z",
  "errors": [
    {
      "field": "code",
      "rejectedValue": null,
      "message": "コードは必須です"
    }
  ]
}
```

#### 文字列長制限

- `code`: 1〜50文字
- `content`: 1〜1000文字

#### パターン制限

- `code`: 英数字、ハイフン、アンダースコアのみ (`^[a-zA-Z0-9_-]+$`)

## バージョニング

### 現在のアプローチ

バージョニングは URL パスに含めません。

```
/api/messages  ✅
/api/v1/messages  ❌（現在は不要）
```

### 将来のバージョニング戦略

Breaking Changes が必要になった場合、以下のいずれかを採用：

#### 1. URL Path Versioning（推奨）

```
/api/v1/messages
/api/v2/messages
```

**利点:**
- 明示的
- キャッシュしやすい
- テストしやすい

#### 2. Header Versioning

```
GET /api/messages
Accept: application/vnd.sandbox.v2+json
```

**利点:**
- URL が変わらない
- より RESTful

### Breaking Changes の定義

以下の変更は Breaking Changes とみなします：

- ❌ 既存フィールドの削除
- ❌ 既存フィールドの型変更
- ❌ 必須フィールドの追加
- ❌ エンドポイント URL の変更
- ❌ HTTP メソッドの変更

以下の変更は Non-Breaking とみなします：

- ✅ 新しいフィールドの追加（オプショナル）
- ✅ 新しいエンドポイントの追加
- ✅ 新しいオプショナルクエリパラメータの追加

## セキュリティ

### 認証・認可（将来実装）

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

JWT (JSON Web Token) による認証を予定。

### Input Validation

すべての入力は検証します：

- Bean Validation (`@Valid`, `@NotNull`, `@Size` etc.)
- Custom validators
- OpenAPI schema validation

### SQL Injection 対策

MyBatis PreparedStatement を使用：

```xml
<select id="findById" resultType="Message">
  SELECT * FROM messages WHERE id = #{id}
</select>
```

❌ 文字列連結は使用しない:
```java
"SELECT * FROM messages WHERE id = " + id  // 危険！
```

### XSS 対策

- JSON レスポンスは自動エスケープ
- HTML レスポンスは Thymeleaf でエスケープ

### CORS（将来設定）

```java
@CrossOrigin(origins = "https://example.com")
```

### Rate Limiting（将来実装）

API Gateway または Spring Cloud Gateway でレート制限を実装予定。

## ベストプラクティス

### 1. 適切な HTTP メソッド使用

```
✅ POST /api/messages          # リソース作成
❌ GET /api/messages/create    # GET で作成は NG
```

### 2. 冪等性の保証

PUT, DELETE は冪等（何度実行しても結果が同じ）である必要があります。

```
DELETE /api/messages/123  # 何度実行しても 204 または 404
```

### 3. Location ヘッダー

POST で新規作成時は Location ヘッダーを返します。

```http
HTTP/1.1 201 Created
Location: /api/messages/123
Content-Type: application/json

{
  "id": 123,
  "code": "MSG_001",
  ...
}
```

### 4. 適切なステータスコード

```java
// ✅ 良い例
return ResponseEntity
    .created(location)
    .body(response);

// ❌ 悪い例
return ResponseEntity
    .ok(response);  // 作成なのに 200 は不適切
```

### 5. エラーメッセージの国際化

将来的に多言語対応を考慮。

```json
{
  "message": "コードは必須です",
  "messageKey": "validation.code.required"
}
```

## テスト

### Contract Testing

OpenAPI 仕様との整合性を自動検証：

```java
@Test
void createMessage_shouldConformToOpenApiSpec() {
  given()
    .filter(validationFilter)
    .contentType(MediaType.APPLICATION_JSON_VALUE)
    .body(requestBody)
  .when()
    .post("/api/messages")
  .then()
    .statusCode(201);
}
```

### Acceptance Testing

Gherkin で定義された受け入れ条件を検証：

```gherkin
Scenario: 有効なデータでメッセージを作成する
  Given 以下のメッセージデータを準備する:
    | code    | content       |
    | MSG_001 | Hello, World! |
  When POST /api/messages を呼び出す
  Then ステータスコード 201 が返される
```

## 関連ドキュメント

- [エラーハンドリング仕様](./error-handling.md)
- [OpenAPI Specification](../../specs/openapi/openapi.yaml)
- [Acceptance Criteria](../../specs/acceptance/)
- [Contract Definitions](../../specs/contracts/)

---

**メンテナンス情報**:
- 最終更新: 2026-01-29
- 更新タイミング: 新しい API パターン追加時、設計方針変更時
