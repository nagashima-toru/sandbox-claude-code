---
name: run-docker
description: Start, stop, and manage Docker development and production environments. Use when setting up the local environment or testing production builds.
---

# Docker 環境実行スキル

Docker Compose を使って開発・本番環境を管理します。

## 起動コマンド

```bash
# 開発モード（ホットリロード）
docker compose up

# 開発モード（バックグラウンド）
docker compose up -d

# 本番モード（最適化ビルド）
docker compose -f docker-compose.yml up
```

スクリプトも使用可能:

```bash
./scripts/docker-dev.sh   # 開発モード
./scripts/docker-prod.sh  # 本番モード
```

## モードの違い

| 項目 | 開発モード | 本番モード |
|-----|-----------|----------|
| コマンド | `docker compose up` | `docker compose -f docker-compose.yml up` |
| 設定ファイル | `docker-compose.yml` + `docker-compose.override.yml` | `docker-compose.yml` のみ |
| ビルドターゲット | `development` | `production` |
| ホットリロード | ✅ あり | ❌ なし |
| ソースコードマウント | ✅ あり | ❌ なし |
| アクセス方法 | Frontend: `:3000`, Backend: `:8080` | Nginx のみ: `:80` |
| デバッグポート | 5005 | なし |

## ポート一覧（開発モード）

| サービス | ポート |
|---------|--------|
| Frontend | 3000 |
| Backend | 8080 |
| Nginx | 80 |
| Debug | 5005 |

## よく使うコマンド

```bash
# ログ確認
docker compose logs -f
docker compose logs -f frontend
docker compose logs -f backend

# 特定サービスを再ビルド
docker compose build backend
docker compose build frontend

# 停止・削除
docker compose down

# 停止・ボリューム削除（DB リセット）
docker compose down -v
```

## 設定ファイル構成

```
project-root/
├── docker-compose.yml           # ベース設定（本番相当）
├── docker-compose.override.yml  # 開発用オーバーライド（自動適用）
└── nginx.conf                   # リバースプロキシ設定
```

**なぜ Nginx?**: `NEXT_PUBLIC_API_URL` はビルド時にバンドルされるため、Docker 内部ホスト名（`http://backend:8080`）はブラウザから解決できない。Nginx を挟むことで単一アクセスポイントを提供し、CORS 問題も解消。

## 環境変数設定

`frontend/.env.local` を使用（開発モードで自動マウント）:

```bash
cp frontend/.env.local.example frontend/.env.local
vim frontend/.env.local
```

優先度（高→低）:

1. `docker-compose.override.yml`
2. `.env.local`
3. `docker-compose.yml`

**注意**: `NEXT_PUBLIC_*` 変数はビルド時に埋め込まれるため、変更後は `docker compose build frontend` が必要。

## npm パッケージの追加

```bash
# 1. package.json を編集
# 2. 実行中コンテナ内でインストール（再ビルド不要）
docker exec sandbox-frontend pnpm install
```

## トラブルシューティング

| 症状 | 解決策 |
|-----|--------|
| ホットリロードが効かない | 開発モードで起動確認、`WATCHPACK_POLLING: "true"` 設定確認 |
| API 接続エラー（開発） | `NEXT_PUBLIC_API_URL: http://localhost:8080` を確認 |
| API 接続エラー（本番） | Nginx 経由（`:80`）でアクセス、`:3000` は公開なし |
| パーミッションエラー | `sudo chown -R $(id -u):$(id -g) frontend/` |
| ポート競合 | `lsof -i :8080`、他のコンテナ停止または ports 変更 |
| 古いデータが残る | `docker compose down -v && docker compose build --no-cache && docker compose up` |
