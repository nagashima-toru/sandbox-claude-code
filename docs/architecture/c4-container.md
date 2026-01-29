# C4 Container 図 - コンテナ構成

## 概要

システム内部のコンテナ（アプリケーション、データストア）とそれらの関係を示します。

## コンテナ図

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Container Diagram                                    │
│                    Sandbox Web Application                                   │
└─────────────────────────────────────────────────────────────────────────────┘


         ┌──────────────┐
         │              │
         │   Web User   │
         │              │
         └──────┬───────┘
                │
                │ HTTPS
                │ (Views pages)
                ▼
    ┌───────────────────────────────────────┐
    │                                       │
    │   Next.js Web Application             │  ← Frontend Container
    │   [Container: Node.js + React]        │
    │                                       │
    │   • Server-Side Rendering             │
    │   • Static Site Generation            │
    │   • API Client (Orval generated)      │
    │   • State Management (TanStack Query) │
    │                                       │
    │   Technology:                         │
    │   - Next.js 16                        │
    │   - TypeScript 5                      │
    │   - Tailwind CSS                      │
    │   - shadcn/ui                         │
    │                                       │
    └─────────────┬─────────────────────────┘
                  │
                  │ JSON/HTTPS
                  │ (REST API Calls)
                  │ Content-Type: application/json
                  │
                  ▼
    ┌───────────────────────────────────────┐
    │                                       │
    │   Spring Boot API Application         │  ← Backend Container
    │   [Container: Java 25 + Spring Boot]  │
    │                                       │
    │   • REST API Endpoints                │
    │   • OpenAPI Generated Controllers     │
    │   • Clean Architecture Layers         │
    │   • RFC 7807 Error Handling           │
    │                                       │
    │   Layers:                             │
    │   ┌───────────────────────────┐       │
    │   │ Presentation              │       │
    │   │ - Controllers (Generated) │       │
    │   │ - DTOs (Generated)        │       │
    │   │ - Exception Handlers      │       │
    │   └───────────┬───────────────┘       │
    │               │                       │
    │   ┌───────────▼───────────────┐       │
    │   │ Application               │       │
    │   │ - Use Cases               │       │
    │   │ - Service Orchestration   │       │
    │   └───────────┬───────────────┘       │
    │               │                       │
    │   ┌───────────▼───────────────┐       │
    │   │ Domain                    │       │
    │   │ - Business Logic          │       │
    │   │ - Domain Models           │       │
    │   │ - Repository Interfaces   │       │
    │   └───────────┬───────────────┘       │
    │               │                       │
    │   ┌───────────▼───────────────┐       │
    │   │ Infrastructure            │       │
    │   │ - MyBatis Mappers         │       │
    │   │ - Repository Impl         │       │
    │   └───────────────────────────┘       │
    │                                       │
    │   Technology:                         │
    │   - Spring Boot 3.5                   │
    │   - Java 25                           │
    │   - MyBatis                           │
    │   - Flyway                            │
    │                                       │
    └─────────────┬─────────────────────────┘
                  │
                  │ JDBC
                  │ (SQL Queries)
                  │ jdbc:postgresql://
                  │
                  ▼
    ┌───────────────────────────────────────┐
    │                                       │
    │   PostgreSQL Database                 │  ← Data Store
    │   [Container: PostgreSQL 16]          │
    │                                       │
    │   • Relational Database               │
    │   • ACID Transactions                 │
    │   • Connection Pooling (HikariCP)     │
    │                                       │
    │   Tables:                             │
    │   - messages                          │
    │                                       │
    │   Technology:                         │
    │   - PostgreSQL 16                     │
    │                                       │
    └───────────────────────────────────────┘


    ┌─────────────────────────────────────────────────────────┐
    │  Supporting Containers (Development/CI)                 │
    └─────────────────────────────────────────────────────────┘

    ┌───────────────────┐         ┌───────────────────┐
    │   Swagger UI      │         │  GitHub Actions   │
    │   [OpenAPI Docs]  │         │  [CI/CD Pipeline] │
    │                   │         │                   │
    │   • API Explorer  │         │  • Spec Validation│
    │   • Interactive   │         │  • Contract Tests │
    │   • Documentation │         │  • OpenAPI Checks │
    │                   │         │                   │
    └───────────────────┘         └───────────────────┘
```

## コンテナ詳細

### 1. Next.js Web Application（Frontend）

**種類**: Web Application

**技術スタック**:
- **Runtime**: Node.js 20+
- **Framework**: Next.js 16 (App Router)
- **Language**: TypeScript 5
- **Styling**: Tailwind CSS 3
- **UI Components**: shadcn/ui
- **State Management**: TanStack Query 5
- **API Client**: Orval (OpenAPI generated)

**責務**:
- ユーザーインターフェースの提供
- Server-Side Rendering (SSR)
- Static Site Generation (SSG)
- API リクエストの送信
- クライアントサイドバリデーション
- 状態管理

**API 通信**:
- **Protocol**: HTTPS
- **Format**: JSON
- **Endpoints**: `/api/*`
- **Authentication**: （将来）JWT Bearer Token

**ポート**:
- Development: 3000
- Production (Docker): 3000

---

### 2. Spring Boot API Application（Backend）

**種類**: API Application

**技術スタック**:
- **Runtime**: JVM (Java 25)
- **Framework**: Spring Boot 3.5.10
- **Architecture**: Clean Architecture
- **ORM**: MyBatis 3.0.4
- **Validation**: Bean Validation (Hibernate Validator)
- **API Generation**: OpenAPI Generator 7.10.0

**アーキテクチャ層**:

#### Presentation Layer
```java
com.sandbox.api.presentation
├── controller/          // REST Controllers
├── dto/                 // 手動定義 DTO
├── generated/           // OpenAPI 生成コード
│   ├── api/            // Controller インターフェース
│   └── model/          // DTO モデル
└── exception/          // Exception Handlers
```

**責務**:
- HTTP リクエスト/レスポンス処理
- リクエストバリデーション
- DTO ↔ Domain モデル変換
- エラーハンドリング（RFC 7807）

#### Application Layer
```java
com.sandbox.api.application
└── usecase/            // Use Case implementations
```

**責務**:
- ビジネスロジックの調整
- トランザクション管理
- ドメインサービスの呼び出し

#### Domain Layer
```java
com.sandbox.api.domain
├── model/              // Domain models
├── repository/         // Repository interfaces
└── exception/          // Domain exceptions
```

**責務**:
- ビジネスルールの実装
- ドメインモデルの管理
- 不変条件の保護

#### Infrastructure Layer
```java
com.sandbox.api.infrastructure
└── persistence/        // MyBatis mappers, Repository impl
```

**責務**:
- データベースアクセス
- Repository インターフェースの実装
- SQL マッピング

**API エンドポイント**:
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/messages | メッセージ一覧取得（ページネーション） |
| GET | /api/messages/{id} | メッセージ取得 |
| POST | /api/messages | メッセージ作成 |
| PUT | /api/messages/{id} | メッセージ更新 |
| DELETE | /api/messages/{id} | メッセージ削除 |

**ポート**:
- Development: 8080
- Integration Test: 8081
- Production (Docker): 8080

---

### 3. PostgreSQL Database

**種類**: Relational Database

**技術スタック**:
- **RDBMS**: PostgreSQL 16
- **Connection Pool**: HikariCP
- **Migration**: Flyway

**スキーマ**:

```sql
-- messages テーブル
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_messages_code ON messages(code);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
```

**責務**:
- データ永続化
- ACID トランザクション保証
- インデックスによる高速検索
- データ整合性制約

**ポート**:
- Development: 5432
- Test: 5432

---

### 4. Swagger UI（開発サポート）

**種類**: Documentation Tool

**技術**:
- springdoc-openapi-ui
- OpenAPI 3.0

**責務**:
- API ドキュメント提供
- インタラクティブ API Explorer
- リクエスト/レスポンス例示

**アクセス**:
- URL: `http://localhost:8080/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/api-docs.yaml`

---

### 5. GitHub Actions（CI/CD）

**種類**: CI/CD Pipeline

**ワークフロー**:

#### Backend CI
- ビルド・テスト
- コードカバレッジチェック（80%以上）
- SpotBugs 静的解析
- Checkstyle コードスタイルチェック
- OWASP 依存関係チェック
- **OpenAPI Validation Tests** ← Phase 3 追加
- **Contract Tests** ← Phase 3 追加

#### Frontend CI
- Lint（ESLint）
- Type Check（TypeScript）
- Unit Tests（Vitest）
- Build Verification
- E2E Tests（Playwright）

#### Spec Validation ← Phase 4 追加
- Spectral による OpenAPI lint
- Gherkin 構文チェック
- Breaking Changes 検出

**成果物**:
- Test Reports
- Coverage Reports
- Security Reports
- API Documentation

---

## データフロー詳細

### メッセージ作成フロー

```
1. User Input
   ↓
2. Frontend: Form Validation
   ↓
3. Frontend: POST /api/messages
   Content-Type: application/json
   Body: { code: "MSG_001", content: "..." }
   ↓
4. Backend: Controller (Presentation)
   - @Valid によるバリデーション
   - DTO → Domain Model 変換
   ↓
5. Backend: Use Case (Application)
   - ビジネスロジック実行
   - トランザクション開始
   ↓
6. Backend: Domain Model (Domain)
   - ビジネスルール検証
   - 不変条件チェック
   ↓
7. Backend: Repository (Infrastructure)
   - MyBatis Mapper 呼び出し
   - INSERT SQL 実行
   ↓
8. Database: PostgreSQL
   - データ挿入
   - UNIQUE 制約チェック
   - トランザクションコミット
   ↓
9. Backend: Response
   - Domain Model → DTO 変換
   - 201 Created
   - Location Header: /api/messages/{id}
   ↓
10. Frontend: Response Handling
    - Cache 更新（TanStack Query）
    - UI 更新
    - リダイレクト
```

## コンテナ間通信

### Protocol & Format

| From | To | Protocol | Format | Auth |
|------|-----|----------|--------|------|
| Frontend | Backend | HTTPS | JSON | （将来）JWT |
| Backend | Database | TCP | PostgreSQL Protocol | Password |
| Browser | Frontend | HTTPS | HTML/JSON | - |
| Developer | Swagger UI | HTTP | HTML | - |

### Error Handling

すべての API エラーレスポンスは RFC 7807 Problem Details 形式：

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

## デプロイメント構成

### Development (Docker Compose)

```yaml
services:
  postgres:    # Database Container
  backend:     # Spring Boot Container
  frontend:    # Next.js Container
```

### Production (Docker Compose)

```yaml
services:
  postgres:    # Database Container
  backend:     # Spring Boot Container
  frontend:    # Next.js Container (Build)
  nginx:       # Reverse Proxy
```

## 関連ドキュメント

- [C4 Context 図](./c4-context.md) - システム全体像
- [アーキテクチャ概要](./README.md) - 原則とスタック
- [Test Strategy](../../backend/docs/TEST_STRATEGY.md) - テスト戦略
- [Docker Deployment](../DOCKER_DEPLOYMENT.md) - デプロイ詳細

---

**メンテナンス情報**:
- 最終更新: 2026-01-29
- 更新タイミング: 新しいコンテナ追加時、技術スタック変更時
