#!/bin/bash

set -e

echo "🚀 Starting backend services for E2E tests..."

# Navigate to project root
cd "$(dirname "$0")/../.."

# Stop any existing containers
echo "🧹 Cleaning up existing containers..."
docker compose -f docker-compose.yml -f docker-compose.override.yml -f docker-compose.tmpfs.yml -f docker-compose.e2e.yml --env-file .env.e2e.example down -v 2>/dev/null || true

# Build and start services
echo "🏗️  Building and starting services..."
docker compose -f docker-compose.yml -f docker-compose.override.yml -f docker-compose.tmpfs.yml -f docker-compose.e2e.yml --env-file .env.e2e.example up -d --build

# Wait for backend to be healthy
echo "⏳ Waiting for backend to be ready..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  if curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "✅ Backend is ready!"
    exit 0
  fi

  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo "   Attempt $RETRY_COUNT/$MAX_RETRIES - Backend not ready yet, waiting..."
  sleep 2
done

echo "❌ Backend failed to start within expected time"
echo "📋 Backend logs:"
docker compose -f docker-compose.yml -f docker-compose.override.yml -f docker-compose.tmpfs.yml -f docker-compose.e2e.yml --env-file .env.e2e.example logs backend
exit 1
