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
Epic ベースブランチ作成
  ↓
👉 仕様 PR 作成（Epic branch へ）
  ↓
レビュー・マージ（Epic branch へ）
  ↓
spec-approved
  ↓
実装計画策定
  ↓
Story実装（Epic branch へ）
  ↓
Epic PR作成（master へ）
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

## 3. 空実装の追加（ビルドエラー回避）

**重要**: 仕様PRには OpenAPI 仕様と受け入れ条件だけでなく、**空実装**も含めます。

### なぜ空実装が必要か？

OpenAPI 仕様から生成されるインターフェース（例: `AuthApi.java`）は、既存の Controller に新しいメソッドの実装を強制します。仕様だけを Epic ブランチにマージすると、**Epic ブランチがビルドエラー**になります。

```java
// ❌ 仕様PRだけマージした場合（ビルドエラー）

// OpenAPI から自動生成される
public interface AuthApi {
  ResponseEntity<LoginResponse> login(...);
  ResponseEntity<UserResponse> getCurrentUser();  // ← 追加
}

// 既存のController
@RestController
public class AuthController implements AuthApi {
  public ResponseEntity<LoginResponse> login(...) { ... }
  // getCurrentUser() がない → コンパイルエラー！
}
```

### 空実装の例

**Backend (Java/Spring Boot)**:

```java
// AuthController.java に追加
@Override
public ResponseEntity<UserResponse> getCurrentUser() {
  throw new UnsupportedOperationException(
    "Not implemented yet - Story 3: 現在のユーザー情報取得"
  );
}
```

**Frontend (TypeScript/Next.js)**:

```typescript
// 必要に応じて、対応するコンポーネントやページに空実装を追加
export default function CurrentUserPage() {
  return (
    <div>
      <h1>Not implemented yet - Story 3</h1>
    </div>
  );
}
```

### 空実装のメリット

| メリット | 説明 |
|---------|------|
| ✅ Epic ブランチが常にビルド可能 | CI/CD が正しく機能する |
| ✅ 何を実装すべきか明確 | Story 実装者が空実装を検索して置き換えるだけ |
| ✅ 仕様承認プロセスは維持 | PR でレビュー・承認できる |
| ✅ レビュアーの混乱を防ぐ | ビルドエラーの原因を探す必要がない |

### 空実装を置き換えるタイミング

Story 実装時に、空実装を実際の実装で置き換えます：

```java
// Story 3 実装時
@Override
public ResponseEntity<UserResponse> getCurrentUser() {
  // 空実装を削除し、実際の実装に置き換え
  UserResponse response = getCurrentUserUseCase.execute();
  return ResponseEntity.ok(AuthMapper.toGenerated(response));
}
```

---

## 作成手順

### Step 1: Epic ベースブランチを作成

```bash
git checkout master
git pull
git checkout -b feature/issue-88-auth
git push -u origin feature/issue-88-auth
```

### Step 2: 仕様ブランチを作成

```bash
git checkout feature/issue-88-auth
git checkout -b feature/issue-88-auth-spec
```

### Step 3: OpenAPI 仕様を追加

`specs/openapi/openapi.yaml` にエンドポイントを追加

### Step 4: 受け入れ条件を作成

```bash
mkdir -p specs/acceptance/auth
# .feature ファイルを作成
```

### Step 5: 空実装を追加（重要）

**Backend**:

```java
// 既存の Controller に空実装を追加
@Override
public ResponseEntity<UserResponse> getCurrentUser() {
  throw new UnsupportedOperationException(
    "Not implemented yet - Story 3: 現在のユーザー情報取得"
  );
}
```

**Frontend** (必要に応じて):

```typescript
// 対応するページやコンポーネントに空実装を追加
export default function CurrentUserPage() {
  return <div>Not implemented yet - Story 3</div>;
}
```

### Step 6: テストとビルド確認

```bash
# Backend
cd backend && ./mvnw verify

# Frontend
cd frontend && pnpm build
```

**重要**: 仕様PRをマージする前に、Epic ブランチがビルド可能であることを確認します。

### Step 7: PR 作成（Epic branch へ）

```bash
gh pr create --base feature/issue-88-auth \
             --head feature/issue-88-auth-spec \
             --template .github/PULL_REQUEST_TEMPLATE/spec.md \
             --label spec

# PR タイトル例: "[Spec] Epic #88 仕様定義"
```

**PR 本文の必須項目**:

- `Story: #88`（Issue 番号）
- 変更内容の概要
- レビュー観点

**重要**: 仕様PRは **Epic ベースブランチ** (`feature/issue-88-auth`) にマージします。masterのビルドを保護するため、実装完了まで仕様はEpicブランチ内に留めます。

---

## レビュー・マージ後の作業

### 1. Issue に仕様を明記

Issue #88 に以下のコメントを追加：

```markdown
## ✅ 仕様承認完了

仕様 PR #XX が Epic ベースブランチにマージされ、仕様が確定しました。

### 承認された仕様

**仕様 PR**: #XX (Epic branch へマージ済み)

**Epic ベースブランチ**: `feature/issue-88-auth`

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
- [ ] Story 実装開始（Epic ブランチから分岐）
- [ ] 全 Story 完了後、Epic PR で master へマージ
```

### 2. spec-approved ラベル付与

Issue #88 に `spec-approved` ラベルを手動で付与

### 3. 実装計画策定

Issue のコメントを参照しながら `.epic/` を作成

---

## Epic ベースブランチへのマージの利点

**なぜ master ではなく Epic ブランチにマージするのか？**

1. **master のビルド保護**
   - OpenAPI 仕様から自動生成されるインターフェース (例: `AuthApi.java`) は、既存の Controller に実装を強制する
   - 仕様だけを master にマージすると、実装がないためビルドエラーになる
   - Epic ブランチにマージすることで、master は常にビルド可能な状態を保つ

2. **仕様承認プロセスの維持**
   - GitHub の PR Approve 機能で正式に仕様承認できる
   - レビュアーは仕様だけに集中してレビュー可能

3. **柔軟な仕様変更**
   - 実装中に仕様変更が必要になった場合、Epic ブランチ内で調整できる
   - master への影響なし

4. **一貫性の保証**
   - Epic 全体（仕様 + 全 Story 実装）をまとめて master にマージ
   - 仕様と実装の不整合が発生しない

---

## チェックリスト

仕様 PR 作成前に確認：

- [ ] すべてのエンドポイントが OpenAPI に定義されている
- [ ] すべてのエンドポイントに受け入れ条件がある
- [ ] 正常系と異常系の両方がカバーされている
- [ ] エラーレスポンスが RFC 7807 形式で定義されている
- [ ] 認証が必要なエンドポイントに security が設定されている
- [ ] Breaking Changes がある場合、明記されている
- [ ] **空実装が追加されている**（Backend Controller など）
- [ ] **Epic ブランチでビルドが成功する**（`./mvnw verify` または `pnpm build`）

---

## 参考資料

- [OpenAPI Specification](https://swagger.io/specification/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
- [RFC 7807 - Problem Details](https://tools.ietf.org/html/rfc7807)
