#!/bin/bash
# E2E テストをローカル環境で実行するスクリプト
#
# Usage:
#   ./scripts/e2e-test-local.sh [playwright-options]
#
# Examples:
#   ./scripts/e2e-test-local.sh                    # Run all E2E tests
#   ./scripts/e2e-test-local.sh permission-ui      # Run permission-ui tests only
#   ./scripts/e2e-test-local.sh --ui               # Run with Playwright UI
#
# Requirements:
#   - Docker (for PostgreSQL)
#   - Java 25 (for backend)
#   - Node.js (for frontend)

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# State tracking
POSTGRES_STARTED=false
BACKEND_STARTED=false
BACKEND_PID=""

# Cleanup function
cleanup() {
  echo ""
  echo -e "${BLUE}🧹 Cleaning up...${NC}"

  if [ "$BACKEND_STARTED" = true ] && [ -n "$BACKEND_PID" ]; then
    echo -e "${YELLOW}🛑 Stopping backend (PID: $BACKEND_PID)...${NC}"
    kill "$BACKEND_PID" 2>/dev/null || true
    wait "$BACKEND_PID" 2>/dev/null || true
  fi

  if [ "$POSTGRES_STARTED" = true ]; then
    echo -e "${YELLOW}🛑 Stopping PostgreSQL...${NC}"
    docker compose down postgres
  fi

  echo -e "${GREEN}✅ Cleanup complete${NC}"
}

# Set trap for cleanup on exit
trap cleanup EXIT INT TERM

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                            ║${NC}"
echo -e "${BLUE}║    E2E Test Local Execution Script         ║${NC}"
echo -e "${BLUE}║                                            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""

# Check PostgreSQL availability
echo -e "${BLUE}🔍 Checking PostgreSQL...${NC}"
if pg_isready -h localhost -p 5432 -q 2>/dev/null; then
  echo -e "${GREEN}✅ PostgreSQL is already running${NC}"
else
  echo -e "${YELLOW}📦 Starting PostgreSQL with Docker Compose...${NC}"
  docker compose up postgres -d
  POSTGRES_STARTED=true

  # Wait for PostgreSQL to be ready
  echo -e "${YELLOW}⏳ Waiting for PostgreSQL to be ready...${NC}"
  for i in {1..30}; do
    if pg_isready -h localhost -p 5432 -q 2>/dev/null; then
      echo -e "${GREEN}✅ PostgreSQL is ready${NC}"
      break
    fi
    if [ $i -eq 30 ]; then
      echo -e "${RED}❌ PostgreSQL failed to start within 30 seconds${NC}"
      exit 1
    fi
    sleep 1
  done
fi

echo ""

# Check backend availability
echo -e "${BLUE}🔍 Checking backend...${NC}"
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
  echo -e "${GREEN}✅ Backend is already running${NC}"
else
  echo -e "${YELLOW}🔧 Starting backend...${NC}"
  cd backend
  ./mvnw spring-boot:run > ../backend-e2e.log 2>&1 &
  BACKEND_PID=$!
  BACKEND_STARTED=true
  cd ..

  # Wait for backend to be ready
  echo -e "${YELLOW}⏳ Waiting for backend to be ready...${NC}"
  for i in {1..60}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
      echo -e "${GREEN}✅ Backend is ready (PID: $BACKEND_PID)${NC}"
      break
    fi
    if [ $i -eq 60 ]; then
      echo -e "${RED}❌ Backend failed to start within 60 seconds${NC}"
      echo -e "${YELLOW}Check backend-e2e.log for details${NC}"
      exit 1
    fi
    sleep 1
  done
fi

echo ""

# Run E2E tests
echo -e "${BLUE}🧪 Running E2E tests...${NC}"
echo ""

cd frontend
if pnpm test:e2e "$@"; then
  echo ""
  echo -e "${GREEN}╔════════════════════════════════════════════╗${NC}"
  echo -e "${GREEN}║                                            ║${NC}"
  echo -e "${GREEN}║    ✅ All E2E tests passed!                ║${NC}"
  echo -e "${GREEN}║                                            ║${NC}"
  echo -e "${GREEN}╚════════════════════════════════════════════╝${NC}"
  EXIT_CODE=0
else
  echo ""
  echo -e "${RED}╔════════════════════════════════════════════╗${NC}"
  echo -e "${RED}║                                            ║${NC}"
  echo -e "${RED}║    ❌ E2E tests failed                     ║${NC}"
  echo -e "${RED}║                                            ║${NC}"
  echo -e "${RED}╚════════════════════════════════════════════╝${NC}"
  EXIT_CODE=1
fi
cd ..

echo ""

exit $EXIT_CODE
