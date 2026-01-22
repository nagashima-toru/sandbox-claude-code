#!/bin/bash

################################################################################
# Frontend CI Verification Script
#
# This script replicates the GitHub Actions frontend CI workflow locally.
# It ensures that if this passes locally, CI should pass.
#
# Usage:
#   ./scripts/frontend/ci-verify.sh [OPTIONS]
#
# Options:
#   --e2e               Run E2E tests (slow, requires Docker)
#   --parallel          Run lint and type-check in parallel (faster)
#   --skip-install      Skip dependency installation (faster iteration)
#   --verbose           Show detailed output
#   --help              Show this help message
#
# Exit codes:
#   0  - All checks passed
#   11 - ESLint failure
#   12 - Prettier failure
#   13 - TypeScript error
#   21 - Coverage below threshold
#   30 - Build failure
#   40 - E2E test failure
#   99 - Environment validation failed
################################################################################

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Default options
RUN_E2E=false
RUN_PARALLEL=false
SKIP_INSTALL=false
VERBOSE=false

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --e2e)
      RUN_E2E=true
      shift
      ;;
    --parallel)
      RUN_PARALLEL=true
      shift
      ;;
    --skip-install)
      SKIP_INSTALL=true
      shift
      ;;
    --verbose)
      VERBOSE=true
      shift
      ;;
    --help)
      head -n 35 "$0" | grep "^#" | sed 's/^# //; s/^#//'
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      echo "Use --help for usage information"
      exit 1
      ;;
  esac
done

# Helper functions
print_header() {
  echo ""
  echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${BOLD}${BLUE}$1${NC}"
  echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo ""
}

print_success() {
  echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
  echo -e "${RED}❌ $1${NC}"
}

print_warning() {
  echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
  echo -e "${BLUE}ℹ️  $1${NC}"
}

# Change to frontend directory
cd "$(dirname "$0")/../../frontend" || exit 1

################################################################################
# 1. Environment Validation
################################################################################

print_header "1. Environment Validation"

# Check Node version
print_info "Checking Node.js version..."
if command -v node &> /dev/null; then
  NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
  if [ "$NODE_VERSION" = "20" ]; then
    print_success "Node.js 20 detected"
  else
    print_warning "Node.js 20 recommended, but Node $NODE_VERSION detected"
    print_info "CI uses Node 20. Consider using nvm to switch versions."
  fi
else
  print_error "Node.js not found. Please install Node.js 20"
  exit 99
fi

# Check pnpm
print_info "Checking pnpm..."
if command -v pnpm &> /dev/null; then
  print_success "pnpm is installed"
else
  print_error "pnpm not found. Please install pnpm 9"
  print_info "  npm install -g pnpm@9"
  exit 99
fi

print_success "Environment validation passed"

################################################################################
# 2. Install Dependencies
################################################################################

if [ "$SKIP_INSTALL" = false ]; then
  print_header "2. Install Dependencies"

  print_info "Running: pnpm install --frozen-lockfile"

  if [ "$VERBOSE" = true ]; then
    pnpm install --frozen-lockfile
  else
    pnpm install --frozen-lockfile > /tmp/pnpm-install.log 2>&1
  fi

  print_success "Dependencies installed"

  print_info "Generating API client..."
  if [ "$VERBOSE" = true ]; then
    pnpm generate:api
  else
    pnpm generate:api > /tmp/pnpm-generate-api.log 2>&1
  fi

  print_success "API client generated"
else
  print_header "2. Install Dependencies (SKIPPED)"
  print_warning "Skipping dependency installation (--skip-install)"
  print_info "Make sure dependencies are up to date!"
fi

################################################################################
# 3. Lint and Type-Check
################################################################################

if [ "$RUN_PARALLEL" = true ]; then
  print_header "3. Lint and Type-Check (Parallel)"

  print_info "Running lint and type-check in parallel..."

  # Run in background
  (
    if [ "$VERBOSE" = true ]; then
      pnpm lint
    else
      pnpm lint > /tmp/pnpm-lint.log 2>&1
    fi
    echo $? > /tmp/lint-exit-code
  ) &
  LINT_PID=$!

  (
    if [ "$VERBOSE" = true ]; then
      pnpm type-check
    else
      pnpm type-check > /tmp/pnpm-type-check.log 2>&1
    fi
    echo $? > /tmp/type-check-exit-code
  ) &
  TYPE_CHECK_PID=$!

  # Wait for both to complete
  wait $LINT_PID
  wait $TYPE_CHECK_PID

  # Check results
  LINT_EXIT_CODE=$(cat /tmp/lint-exit-code)
  TYPE_CHECK_EXIT_CODE=$(cat /tmp/type-check-exit-code)

  if [ $LINT_EXIT_CODE -ne 0 ]; then
    print_error "Lint failed"
    if [ "$VERBOSE" = false ]; then
      echo ""
      cat /tmp/pnpm-lint.log
      echo ""
    fi
    exit 11
  fi

  if [ $TYPE_CHECK_EXIT_CODE -ne 0 ]; then
    print_error "Type-check failed"
    if [ "$VERBOSE" = false ]; then
      echo ""
      cat /tmp/pnpm-type-check.log
      echo ""
    fi
    exit 13
  fi

  print_success "Lint and type-check passed"

else
  # Run sequentially
  print_header "3. Lint"

  print_info "Running: pnpm lint"
  if [ "$VERBOSE" = true ]; then
    pnpm lint
  else
    if ! pnpm lint > /tmp/pnpm-lint.log 2>&1; then
      print_error "ESLint failed"
      echo ""
      cat /tmp/pnpm-lint.log
      echo ""
      exit 11
    fi
  fi

  print_success "ESLint passed"

  print_info "Running: pnpm format:check"
  if [ "$VERBOSE" = true ]; then
    pnpm format:check
  else
    if ! pnpm format:check > /tmp/pnpm-format-check.log 2>&1; then
      print_error "Prettier check failed"
      echo ""
      cat /tmp/pnpm-format-check.log
      echo ""
      print_info "Run 'pnpm format' to fix formatting issues"
      exit 12
    fi
  fi

  print_success "Prettier check passed"

  print_header "4. Type-Check"

  print_info "Running: pnpm type-check"
  if [ "$VERBOSE" = true ]; then
    pnpm type-check
  else
    if ! pnpm type-check > /tmp/pnpm-type-check.log 2>&1; then
      print_error "TypeScript type check failed"
      echo ""
      cat /tmp/pnpm-type-check.log
      echo ""
      exit 13
    fi
  fi

  print_success "Type-check passed"
fi

################################################################################
# 5. Test with Coverage
################################################################################

print_header "5. Test with Coverage"

print_info "Running: pnpm test:coverage --passWithNoTests"

if [ "$VERBOSE" = true ]; then
  pnpm test:coverage --passWithNoTests
  TEST_EXIT_CODE=$?
else
  pnpm test:coverage --passWithNoTests > /tmp/pnpm-test.log 2>&1
  TEST_EXIT_CODE=$?
fi

if [ $TEST_EXIT_CODE -ne 0 ]; then
  print_error "Tests failed"
  if [ "$VERBOSE" = false ]; then
    echo ""
    cat /tmp/pnpm-test.log
    echo ""
  fi
  exit 21
fi

print_success "Tests passed"

# Parse coverage report
COVERAGE_JSON="coverage/coverage-summary.json"

if [ -f "$COVERAGE_JSON" ]; then
  print_info "Parsing coverage report..."

  # Extract coverage data
  if command -v jq &> /dev/null; then
    STATEMENTS=$(jq -r '.total.statements.pct' "$COVERAGE_JSON")
    BRANCHES=$(jq -r '.total.branches.pct' "$COVERAGE_JSON")
    FUNCTIONS=$(jq -r '.total.functions.pct' "$COVERAGE_JSON")
    LINES=$(jq -r '.total.lines.pct' "$COVERAGE_JSON")
  else
    # Fallback: parse JSON manually (macOS doesn't have jq by default)
    STATEMENTS=$(grep -o '"statements":{[^}]*"pct":[0-9.]*' "$COVERAGE_JSON" | grep -o '[0-9.]*$')
    BRANCHES=$(grep -o '"branches":{[^}]*"pct":[0-9.]*' "$COVERAGE_JSON" | grep -o '[0-9.]*$')
    FUNCTIONS=$(grep -o '"functions":{[^}]*"pct":[0-9.]*' "$COVERAGE_JSON" | grep -o '[0-9.]*$')
    LINES=$(grep -o '"lines":{[^}]*"pct":[0-9.]*' "$COVERAGE_JSON" | grep -o '[0-9.]*$')
  fi

  # Display coverage table
  echo ""
  echo "Coverage Report:"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  printf "%-20s %10s %10s\n" "Metric" "Coverage" "Threshold"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

  check_coverage() {
    local name=$1
    local actual=$2
    local threshold=$3

    local status
    if (( $(echo "$actual >= $threshold" | awk '{print ($1 >= $3)}') )); then
      status="${GREEN}✅${NC}"
    else
      status="${RED}❌${NC}"
    fi

    printf "%-20s %9s%% %9s%%  %b\n" "$name" "$actual" "$threshold" "$status"
  }

  check_coverage "Statements" "$STATEMENTS" "80.00"
  check_coverage "Branches" "$BRANCHES" "70.00"
  check_coverage "Functions" "$FUNCTIONS" "80.00"
  check_coverage "Lines" "$LINES" "80.00"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""

  # Check thresholds
  COVERAGE_FAILED=false

  if (( $(echo "$STATEMENTS < 80" | awk '{print ($1 < $2)}') )); then
    print_error "Statements coverage ($STATEMENTS%) is below threshold (80%)"
    COVERAGE_FAILED=true
  fi

  if (( $(echo "$BRANCHES < 70" | awk '{print ($1 < $2)}') )); then
    print_error "Branches coverage ($BRANCHES%) is below threshold (70%)"
    COVERAGE_FAILED=true
  fi

  if (( $(echo "$FUNCTIONS < 80" | awk '{print ($1 < $2)}') )); then
    print_error "Functions coverage ($FUNCTIONS%) is below threshold (80%)"
    COVERAGE_FAILED=true
  fi

  if (( $(echo "$LINES < 80" | awk '{print ($1 < $2)}') )); then
    print_error "Lines coverage ($LINES%) is below threshold (80%)"
    COVERAGE_FAILED=true
  fi

  if [ "$COVERAGE_FAILED" = true ]; then
    print_info "View detailed report: frontend/coverage/index.html"
    exit 21
  fi

  print_success "Coverage validation passed"
else
  print_warning "Coverage report not found: $COVERAGE_JSON"
  print_info "Tests may have passed with no tests (--passWithNoTests)"
fi

################################################################################
# 6. Production Build
################################################################################

print_header "6. Production Build"

print_info "Running: pnpm build"

if [ "$VERBOSE" = true ]; then
  pnpm build
  BUILD_EXIT_CODE=$?
else
  pnpm build > /tmp/pnpm-build.log 2>&1
  BUILD_EXIT_CODE=$?
fi

if [ $BUILD_EXIT_CODE -ne 0 ]; then
  print_error "Build failed"
  if [ "$VERBOSE" = false ]; then
    echo ""
    echo "Last 50 lines of build output:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    tail -50 /tmp/pnpm-build.log
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    print_info "Run with --verbose flag to see full output"
  fi
  exit 30
fi

print_success "Production build passed"

# Check build size
if [ -d ".next" ]; then
  BUILD_SIZE=$(du -sh .next | awk '{print $1}')
  print_info "Build size: $BUILD_SIZE"
else
  print_warning ".next directory not found"
fi

################################################################################
# 7. E2E Tests (Optional)
################################################################################

if [ "$RUN_E2E" = true ]; then
  print_header "7. E2E Tests"

  print_warning "E2E tests are SLOW (~3-5 minutes)"
  print_info "Setting up E2E environment..."

  # Go back to project root
  cd ..

  # Check if Docker is available
  if ! command -v docker &> /dev/null; then
    print_error "Docker not found. E2E tests require Docker."
    exit 40
  fi

  # Build backend JAR
  print_info "Building backend JAR..."
  if [ "$VERBOSE" = true ]; then
    cd backend && ./mvnw clean package -DskipTests && cd ..
  else
    cd backend && ./mvnw clean package -DskipTests > /tmp/backend-build.log 2>&1 && cd ..
  fi

  # Build backend Docker image
  print_info "Building backend Docker image..."
  if [ "$VERBOSE" = true ]; then
    docker build -t sandbox-backend:e2e ./backend
  else
    docker build -t sandbox-backend:e2e ./backend > /tmp/docker-build.log 2>&1
  fi

  # Check if .env.e2e.example exists
  if [ ! -f ".env.e2e.example" ]; then
    print_error ".env.e2e.example not found"
    exit 40
  fi

  # Start backend services
  print_info "Starting backend services with Docker Compose..."
  docker compose -f docker-compose.yml \
    -f docker-compose.override.yml \
    -f docker-compose.tmpfs.yml \
    -f docker-compose.e2e.yml \
    --env-file .env.e2e.example up -d

  # Wait for backend to be ready
  print_info "Waiting for backend to be ready..."
  WAIT_COUNT=0
  MAX_WAIT=60
  while [ $WAIT_COUNT -lt $MAX_WAIT ]; do
    if curl -f http://localhost:8081/actuator/health &> /dev/null; then
      print_success "Backend is ready!"
      break
    fi
    sleep 2
    WAIT_COUNT=$((WAIT_COUNT + 1))
  done

  if [ $WAIT_COUNT -ge $MAX_WAIT ]; then
    print_error "Backend failed to start within 120 seconds"
    print_info "Collecting backend logs..."
    docker compose -f docker-compose.yml \
      -f docker-compose.override.yml \
      -f docker-compose.tmpfs.yml \
      -f docker-compose.e2e.yml \
      --env-file .env.e2e.example logs
    docker compose -f docker-compose.yml \
      -f docker-compose.override.yml \
      -f docker-compose.tmpfs.yml \
      -f docker-compose.e2e.yml \
      --env-file .env.e2e.example down -v
    exit 40
  fi

  # Install Playwright browsers
  cd frontend
  print_info "Installing Playwright browsers..."
  if [ "$VERBOSE" = true ]; then
    pnpm exec playwright install --with-deps
  else
    pnpm exec playwright install --with-deps > /tmp/playwright-install.log 2>&1
  fi

  # Run E2E tests
  print_info "Running E2E tests..."
  if [ "$VERBOSE" = true ]; then
    CI=true pnpm test:e2e
    E2E_EXIT_CODE=$?
  else
    CI=true pnpm test:e2e > /tmp/e2e-test.log 2>&1
    E2E_EXIT_CODE=$?
  fi

  # Clean up
  print_info "Stopping backend services..."
  cd ..
  docker compose -f docker-compose.yml \
    -f docker-compose.override.yml \
    -f docker-compose.tmpfs.yml \
    -f docker-compose.e2e.yml \
    --env-file .env.e2e.example down -v

  if [ $E2E_EXIT_CODE -ne 0 ]; then
    print_error "E2E tests failed"
    if [ "$VERBOSE" = false ]; then
      echo ""
      cat /tmp/e2e-test.log
      echo ""
    fi
    print_info "View Playwright report: frontend/playwright-report/index.html"
    exit 40
  fi

  print_success "E2E tests passed"

  cd frontend
fi

################################################################################
# Final Summary
################################################################################

print_header "✅ All Frontend Checks Passed!"

echo ""
echo "Summary:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Lint and format checks passed"
echo "✅ Type checking passed"
echo "✅ Tests passed with coverage"

if [ -f "$COVERAGE_JSON" ]; then
  echo "   - Statements: $STATEMENTS% (≥80%)"
  echo "   - Branches: $BRANCHES% (≥70%)"
  echo "   - Functions: $FUNCTIONS% (≥80%)"
  echo "   - Lines: $LINES% (≥80%)"
fi

echo "✅ Production build succeeded"

if [ "$RUN_E2E" = true ]; then
  echo "✅ E2E tests passed"
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

print_success "Frontend is ready for CI! 🚀"
echo ""

exit 0
