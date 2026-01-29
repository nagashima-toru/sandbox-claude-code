# C4 Context 図 - システムコンテキスト

## 概要

C4 モデルの最上位レベルであるコンテキスト図です。システムと外部アクター（ユーザー、外部システム）の関係を示します。

## システムコンテキスト図

```
┌─────────────────────────────────────────────────────────────────┐
│                     System Context Diagram                       │
│                       Sandbox Application                        │
└─────────────────────────────────────────────────────────────────┘


    ┌──────────────┐
    │              │
    │   Web User   │  ← エンドユーザー
    │              │    ブラウザ経由でアクセス
    └──────┬───────┘
           │
           │ HTTPS
           │ (Views pages, manages messages)
           │
           ▼
    ┌─────────────────────────────────────────┐
    │                                         │
    │      Sandbox Web Application            │
    │                                         │
    │  SDD（仕様駆動開発）ベースの            │
    │  メッセージ管理システム                 │
    │                                         │
    │  • OpenAPI-First 開発                   │
    │  • Clean Architecture                   │
    │  • RFC 7807 エラーハンドリング          │
    │                                         │
    └───────────┬─────────────────────────────┘
                │
                │ JDBC
                │ (Reads/Writes data)
                │
                ▼
    ┌───────────────────────┐
    │                       │
    │   PostgreSQL DB       │  ← データベース
    │                       │    メッセージデータ永続化
    └───────────────────────┘


    External Systems (Future):

    ┌──────────────┐
    │              │
    │  Auth System │  ← 認証システム（今後追加予定）
    │   (Future)   │    JWT トークン発行
    └──────────────┘
```

## 登場人物・システム

### 1. Web User（エンドユーザー）

**種類**: Person

**説明**: Web ブラウザを使用してアプリケーションにアクセスするユーザー

**責務**:
- メッセージの作成・閲覧・編集・削除
- システムとの対話

**使用技術**:
- Web ブラウザ（Chrome, Firefox, Safari, Edge）

---

### 2. Sandbox Web Application（対象システム）

**種類**: Software System

**説明**: SDD（仕様駆動開発）に基づいて構築されたメッセージ管理 Web アプリケーション

**主な機能**:
- メッセージの CRUD 操作
- ページネーション対応の一覧表示
- バリデーション・エラーハンドリング
- OpenAPI 仕様に基づく API 提供

**技術的特徴**:
- **Backend**: Spring Boot (Java 25) - REST API
- **Frontend**: Next.js (TypeScript) - SSR/SPA
- **API**: OpenAPI 3.0 準拠
- **Error Handling**: RFC 7807 Problem Details

**提供する価値**:
- 仕様と実装の一貫性保証
- 自動化されたコード生成と検証
- 高品質な API 契約管理

---

### 3. PostgreSQL Database

**種類**: Software System (External)

**説明**: アプリケーションデータを永続化するリレーショナルデータベース

**責務**:
- メッセージデータの永続化
- トランザクション管理
- データ整合性の保証

**技術**:
- PostgreSQL 16
- HikariCP（Connection Pooling）
- Flyway（Migration Management）

---

### 4. Authentication System（将来追加予定）

**種類**: Software System (External, Future)

**説明**: ユーザー認証・認可を提供する外部システム（将来実装予定）

**責務**:
- ユーザー認証
- JWT トークン発行・検証
- ロール・権限管理

**想定技術**:
- OAuth 2.0 / OpenID Connect
- JWT (JSON Web Tokens)

---

## データフロー

### 主要なユースケース

#### 1. メッセージ作成

```
User → [Web Browser]
    → Sandbox Application (Frontend)
    → Sandbox Application (Backend API)
    → PostgreSQL Database
```

1. ユーザーがフォームに入力
2. Frontend が POST /api/messages リクエスト送信
3. Backend がバリデーション実行
4. Database にデータ保存
5. 201 Created レスポンス返却

#### 2. メッセージ一覧取得

```
User → [Web Browser]
    → Sandbox Application (Frontend)
    → Sandbox Application (Backend API)
    → PostgreSQL Database
```

1. ユーザーが一覧ページにアクセス
2. Frontend が GET /api/messages リクエスト送信
3. Backend がページネーション処理
4. Database からデータ取得
5. 200 OK + JSON レスポンス返却

## セキュリティ境界

### 現在の実装

```
┌─────────────────────────────────────────┐
│  Trust Boundary: Internet               │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ DMZ: Application Server           │  │
│  │                                   │  │
│  │  - Input Validation               │  │
│  │  - SQL Injection Prevention       │  │
│  │  - XSS Protection                 │  │
│  │                                   │  │
│  └─────────────┬─────────────────────┘  │
│                │                         │
└────────────────┼─────────────────────────┘
                 │
                 │ Internal Network
                 │
         ┌───────▼────────┐
         │   Database     │
         │   (Private)    │
         └────────────────┘
```

### 将来の実装

- API Gateway の導入
- Rate Limiting
- WAF（Web Application Firewall）
- DDoS Protection

## 外部依存関係

### 開発時

- **Maven Central**: Java 依存関係
- **npm Registry**: JavaScript/TypeScript 依存関係
- **Docker Hub**: コンテナイメージ

### ランタイム

- **PostgreSQL**: データ永続化
- **（将来）CDN**: 静的アセット配信
- **（将来）Auth Provider**: 認証サービス

## スケーラビリティ考慮事項

### 現在のアーキテクチャ

- モノリシックアプリケーション
- 単一 Database インスタンス
- Docker Compose によるローカル開発

### 将来のスケーリング戦略

#### 水平スケーリング

- Frontend: 複数インスタンスのロードバランシング
- Backend: ステートレス API のスケールアウト
- Database: Read Replica の追加

#### 垂直スケーリング

- より大きな VM/Container への移行
- Database のリソース増強

#### マイクロサービス化（長期的検討）

- メッセージ管理サービスの分離
- 認証・認可サービスの独立
- API Gateway パターンの導入

## 関連ドキュメント

- [C4 Container 図](./c4-container.md) - コンテナレベルの詳細
- [アーキテクチャ概要](./README.md) - 全体像
- [API 設計ガイドライン](../api/README.md) - API 設計原則

---

**メンテナンス情報**:
- 最終更新: 2026-01-29
- 更新タイミング: 新しい外部システム統合時、アーキテクチャ変更時
