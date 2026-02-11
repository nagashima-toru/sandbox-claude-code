#!/bin/bash
set -e

# E2Eテスト用のバックエンド起動スクリプト
# - ポート8081で起動（通常の開発サーバーとポート競合を避ける）
# - PostgreSQLの起動確認
# - バックグラウンドで実行し、PIDを保存

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
PID_FILE="$PROJECT_ROOT/.backend-e2e.pid"

echo "🚀 Starting backend for E2E tests..."

# 既存のバックエンドが起動している場合は停止
if [ -f "$PID_FILE" ]; then
  OLD_PID=$(cat "$PID_FILE")
  if ps -p "$OLD_PID" > /dev/null 2>&1; then
    echo "⚠️  Stopping existing backend process (PID: $OLD_PID)..."
    kill "$OLD_PID" || true
    sleep 2
  fi
  rm -f "$PID_FILE"
fi

# PostgreSQLの起動確認
echo "🔍 Checking PostgreSQL availability..."
if ! command -v pg_isready &> /dev/null; then
  echo "⚠️  pg_isready not found. Assuming PostgreSQL is running..."
else
  if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "❌ PostgreSQL is not running on localhost:5432"
    echo "Please start PostgreSQL:"
    echo "  - Docker: docker compose up postgres -d"
    echo "  - Homebrew: brew services start postgresql@16"
    exit 1
  fi
  echo "✅ PostgreSQL is running"
fi

# バックエンドをポート8081で起動
cd "$BACKEND_DIR"
echo "📦 Starting Spring Boot on port 8081..."
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081" -DskipTests > "$PROJECT_ROOT/backend-e2e.log" 2>&1 &
BACKEND_PID=$!

# PIDを保存
echo "$BACKEND_PID" > "$PID_FILE"
echo "✅ Backend started with PID: $BACKEND_PID"
echo "📝 Log file: $PROJECT_ROOT/backend-e2e.log"
echo "🔧 PID file: $PID_FILE"

# バックエンドが起動するまで少し待つ
echo "⏳ Waiting for backend to start..."
sleep 5

exit 0
