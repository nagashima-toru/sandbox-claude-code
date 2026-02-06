# アーキテクチャ概要

## 目次

- [システム概要](#システム概要)
- [アーキテクチャ原則](#アーキテクチャ原則)
- [技術スタック](#技術スタック)
- [アーキテクチャ図](#アーキテクチャ図)
- [関連ドキュメント](#関連ドキュメント)

## システム概要

Sandbox は SDD（Specification-Driven Development、仕様駆動開発）を導入した Web アプリケーションです。

### 主な特徴

- **仕様駆動開発**: OpenAPI 仕様を起点としたコード生成と検証
- **Clean Architecture**: ドメイン駆動設計に基づいた層分離
- **コントラクトテスト**: Frontend/Backend 間の API 契約を自動検証
- **RFC 7807 準拠**: 標準化されたエラーレスポンス形式

## アーキテクチャ原則

### 1. 仕様ファースト（Specification-First）

すべての API 開発は OpenAPI 仕様の定義から始まります。

```
仕様定義 → コード生成 → 実装 → テスト
```

- 仕様が承認されるまで実装 PR はマージ不可
- OpenAPI 仕様からコードを自動生成
- 仕様と実装の整合性を CI で自動検証

### 2. 関心の分離（Separation of Concerns）

Clean Architecture パターンに従い、各層の責務を明確に分離します。

```
Presentation Layer (Controllers)
    ↓
Application Layer (Use Cases)
    ↓
Domain Layer (Business Logic)
    ↓
Infrastructure Layer (Persistence)
```

**依存関係のルール:**

- 外側の層は内側の層に依存できる
- 内側の層は外側の層に依存してはならない
- Domain 層は他のどの層にも依存しない

### 3. テスト戦略

テストピラミッドに基づいた効率的なテスト配分：

- **Unit Tests**: 70% - 高速、詳細なテスト
- **Integration Tests**: 25% - API・DB 統合テスト
- **E2E Tests**: 5% - クリティカルパスのみ

### 4. API 契約の厳格な管理

- Consumer-Driven Contracts（CDC）によるコントラクトテスト
- OpenAPI 仕様との自動整合性チェック
- Breaking Changes の自動検出

## 技術スタック

### Backend

| カテゴリ | 技術 | バージョン |
|---------|------|-----------|
| Language | Java | 25 |
| Framework | Spring Boot | 3.5.10 |
| ORM | MyBatis | 3.0.4 |
| Database | PostgreSQL | 16 |
| Migration | Flyway | - |
| Testing | JUnit 5, Testcontainers | - |
| Build Tool | Maven | - |

### Frontend

| カテゴリ | 技術 | バージョン |
|---------|------|-----------|
| Framework | Next.js | 15.x |
| Language | TypeScript | 5.x |
| UI Library | React | 19.x |
| Styling | Tailwind CSS | 3.x |
| State Management | TanStack Query | 5.x |
| Testing | Vitest, Playwright | - |
| Package Manager | pnpm | - |

### DevOps & Tools

| カテゴリ | 技術 | 用途 |
|---------|------|------|
| CI/CD | GitHub Actions | 自動テスト・デプロイ |
| Container | Docker, Docker Compose | 開発・本番環境 |
| API Documentation | Swagger UI, Redoc | API ドキュメント生成 |
| Code Generation | OpenAPI Generator, Orval | コード自動生成 |
| Spec Validation | Spectral | OpenAPI lint |
| Contract Testing | Spring Cloud Contract | CDC テスト |

## アーキテクチャ図

### システムコンテキスト図

システム全体の概要については [C4 Context 図](./c4-context.md) を参照してください。

### コンテナ図

各コンポーネント間の関係については [C4 Container 図](./c4-container.md) を参照してください。

## ディレクトリ構造

```
sandbox-claude-code/
├── backend/              # Spring Boot API
│   ├── src/
│   │   ├── main/
│   │   │   └── java/com/sandbox/api/
│   │   │       ├── domain/           # ビジネスロジック
│   │   │       ├── application/      # ユースケース
│   │   │       ├── infrastructure/   # 永続化
│   │   │       └── presentation/     # REST API
│   │   └── test/                     # テスト
│   └── pom.xml
│
├── frontend/             # Next.js App
│   ├── src/
│   │   ├── app/          # App Router
│   │   ├── components/   # React コンポーネント
│   │   └── lib/          # ユーティリティ
│   └── package.json
│
├── specs/                # 仕様定義（Single Source of Truth）
│   ├── openapi/          # OpenAPI 仕様
│   ├── acceptance/       # 受け入れ条件（Gherkin）
│   └── contracts/        # コントラクト定義
│
├── docs/                 # ドキュメント
│   ├── architecture/     # アーキテクチャ
│   ├── api/              # API 設計
│   └── adr/              # 設計判断記録
│
└── scripts/              # ユーティリティスクリプト
```

## データフロー

### API リクエストの処理フロー

```
1. Client Request
   ↓
2. Controller (Presentation Layer)
   - リクエストバリデーション
   - 生成されたインターフェースを実装
   ↓
3. Use Case (Application Layer)
   - ビジネスロジックの調整
   - トランザクション管理
   ↓
4. Domain Model (Domain Layer)
   - ビジネスルールの実行
   - 不変条件の保護
   ↓
5. Repository Interface (Domain Layer)
   - 永続化の抽象化
   ↓
6. Repository Implementation (Infrastructure Layer)
   - MyBatis Mapper 呼び出し
   - データベースアクセス
   ↓
7. Response
   - Domain Model → DTO 変換
   - JSON レスポンス返却
```

## セキュリティ考慮事項

### 実装済み

- ✅ Input Validation（Bean Validation）
- ✅ SQL Injection 対策（MyBatis PreparedStatement）
- ✅ OWASP Dependency-Check（脆弱性スキャン）
- ✅ SpotBugs 静的解析
- ✅ Content-Type 検証

### 今後の実装予定

- ⬜ JWT 認証・認可
- ⬜ CORS 設定
- ⬜ Rate Limiting
- ⬜ API Gateway
- ⬜ Secrets Management

## パフォーマンス考慮事項

### 現在の実装

- Connection Pooling（HikariCP）
- Database Indexing（主キー、外部キー）
- Lazy Loading（必要に応じて）

### 今後の最適化候補

- Redis キャッシング
- CDN 導入
- Database Query 最適化
- Connection Pool チューニング

## 関連ドキュメント

### アーキテクチャ

- [C4 Context 図](./c4-context.md) - システムコンテキスト
- [C4 Container 図](./c4-container.md) - コンテナ構成

### API 設計

- [API 設計ガイドライン](./api/README.md)
- [エラーハンドリング仕様](./api/error-handling.md)

### 設計判断記録（ADR）

- [ADR-0001: OpenAPI-First 開発の採用](../adr/0001-use-openapi-first.md)

### プロジェクトドキュメント

- [Docker Deployment](../environment/DOCKER_DEPLOYMENT.md)
- [Git Worktree](../environment/GIT_WORKTREE.md)
- [Local CI Verification](../quality/LOCAL_CI_VERIFICATION.md)
- [Test Strategy](../../backend/docs/TEST_STRATEGY.md)

## メンテナンス

このドキュメントは以下のタイミングで更新してください：

- 新しい技術スタックの導入時
- アーキテクチャパターンの変更時
- 重要な設計判断が行われた時
- 四半期ごとの定期レビュー

最終更新: 2026-01-29
