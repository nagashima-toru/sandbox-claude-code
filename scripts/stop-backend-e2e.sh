#!/bin/bash
set -e

# E2Eテスト用のバックエンド停止スクリプト

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PID_FILE="$PROJECT_ROOT/.backend-e2e.pid"

echo "🛑 Stopping backend for E2E tests..."

if [ ! -f "$PID_FILE" ]; then
  echo "⚠️  PID file not found: $PID_FILE"
  echo "Backend may not be running or already stopped"
  exit 0
fi

BACKEND_PID=$(cat "$PID_FILE")

if ps -p "$BACKEND_PID" > /dev/null 2>&1; then
  echo "🔻 Stopping backend process (PID: $BACKEND_PID)..."
  kill "$BACKEND_PID" || true

  # プロセスが終了するまで待つ（最大10秒）
  for i in {1..10}; do
    if ! ps -p "$BACKEND_PID" > /dev/null 2>&1; then
      echo "✅ Backend stopped successfully"
      break
    fi
    echo "⏳ Waiting for backend to stop... ($i/10)"
    sleep 1
  done

  # まだ実行中なら強制終了
  if ps -p "$BACKEND_PID" > /dev/null 2>&1; then
    echo "⚠️  Force killing backend process..."
    kill -9 "$BACKEND_PID" || true
  fi
else
  echo "⚠️  Backend process (PID: $BACKEND_PID) is not running"
fi

# PIDファイルとログファイルを削除
rm -f "$PID_FILE"
rm -f "$PROJECT_ROOT/backend-e2e.log"

echo "✅ Cleanup complete"
exit 0
