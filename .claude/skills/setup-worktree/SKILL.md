---
name: setup-worktree
description: Set up a new git worktree environment for parallel development. Use when working on multiple branches simultaneously.
---

# Worktree 環境セットアップスキル

複数のブランチを並行して開発するために git worktree を設定します。

## セットアップ手順

### 1. Worktree を作成

```bash
# 新しいブランチで worktree を作成
git worktree add ../sandbox-feature-auth feature/auth

# 既存ブランチから作成
git worktree add ../sandbox-bugfix-123 bugfix/123
```

### 2. 環境を自動設定（推奨）

```bash
cd ../sandbox-feature-auth
./scripts/setup-worktree-env.sh
```

スクリプトが以下を自動実行:

- ブランチ名からプロジェクト名を生成
- 使用可能なポートオフセットを提案
- `.env` と `frontend/.env.local` を作成
- アクセス URL を表示

### 3. 環境を起動

```bash
docker compose up
```

### 4. 終了時のクリーンアップ

```bash
# コンテナ停止・削除
docker compose down

# ボリューム（DB）も削除
docker compose down -v

# メインに戻って worktree を削除
cd /path/to/main/repo
git worktree remove ../sandbox-feature-auth
```

## PORT_OFFSET 規約

ポート衝突を避けるため、各 worktree に異なるオフセットを割り当てます。

```
実際のポート = ベースポート + PORT_OFFSET
```

| オフセット | Frontend | Backend | PostgreSQL | Debug | Nginx | 推奨用途 |
|----------|----------|---------|------------|-------|-------|---------|
| 0 | 3000 | 8080 | 5432 | 5005 | 80 | メイン開発 |
| 1 | 3001 | 8081 | 5433 | 5006 | 81 | E2E テスト |
| 100 | 3100 | 8180 | 5532 | 5105 | 180 | フィーチャーブランチ 1 |
| 200 | 3200 | 8280 | 5632 | 5205 | 280 | フィーチャーブランチ 2 |
| 300 | 3300 | 8380 | 5732 | 5305 | 380 | フィーチャーブランチ 3 |

**規則**: 100 の倍数を使用（メンタルマッピングを容易にする）。

## COMPOSE_PROJECT_NAME による名前空間分離

Docker Compose は `COMPOSE_PROJECT_NAME` でリソースを分離します:

- コンテナ: `${COMPOSE_PROJECT_NAME}-backend-1`
- ボリューム: `${COMPOSE_PROJECT_NAME}_postgres-data`
- ネットワーク: `${COMPOSE_PROJECT_NAME}_default`

```bash
# 良い名前の例
COMPOSE_PROJECT_NAME=sandbox-feature-auth
COMPOSE_PROJECT_NAME=sandbox-bugfix-user-login
COMPOSE_PROJECT_NAME=sandbox-pr-123
```

## .env ファイルの手動設定

```bash
COMPOSE_PROJECT_NAME=sandbox-feature-auth
PORT_OFFSET=100
FRONTEND_PORT=3100
BACKEND_PORT=8180
POSTGRES_PORT=5532
DEBUG_PORT=5105
NGINX_PORT=180
POSTGRES_DB=sandbox
POSTGRES_USER=sandbox
POSTGRES_PASSWORD=sandbox
DB_PERSISTENCE_MODE=volume
```

`frontend/.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8180
```

## データベース分離

**Volume モード**（デフォルト）: データは `${COMPOSE_PROJECT_NAME}_postgres-data` に永続化。

**Tmpfs モード**（短命環境向け）:

```bash
# .env に設定
DB_PERSISTENCE_MODE=tmpfs
```

PR レビューや一時的なテストに最適。停止時にデータは自動削除。

## トラブルシューティング

| 症状 | 解決策 |
|-----|--------|
| ポート競合 | `./scripts/check-ports.sh` で確認、別のオフセットを選択 |
| コンテナ名衝突 | `COMPOSE_PROJECT_NAME` が一意かを確認 |
| Frontend が Backend に接続できない | `frontend/.env.local` の `NEXT_PUBLIC_API_URL` が `BACKEND_PORT` と一致しているか確認 |
| PORT_OFFSET 変更後にポートが変わらない | `./scripts/setup-worktree-env.sh --regenerate` を実行して再ビルド |

```bash
# 全環境のポート確認
./scripts/check-ports.sh

# 全 sandbox ボリューム確認
docker volume ls | grep sandbox
```
