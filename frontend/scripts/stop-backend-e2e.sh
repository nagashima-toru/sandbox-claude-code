#!/bin/bash

set -e

echo "🛑 Stopping backend services for E2E tests..."

# Navigate to project root
cd "$(dirname "$0")/../.."

# Stop and remove containers
docker-compose -f docker-compose.e2e.yml down -v

echo "✅ Backend services stopped and cleaned up"
