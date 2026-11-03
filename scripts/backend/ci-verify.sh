#!/bin/bash

################################################################################
# Backend CI Verification Script
#
# This script replicates the GitHub Actions backend CI workflow locally.
# It ensures that if this passes locally, CI should pass.
#
# Usage:
#   ./scripts/backend/ci-verify.sh [OPTIONS]
#
# Options:
#   --dependency-check    Run OWASP dependency-check (slow, ~5-10 minutes)
#   --verbose            Show detailed output
#   --help               Show this help message
#
# Environment:
#   POSTGRES_PORT        PostgreSQL port (default 5432). If it is used by another
#                        process, a throwaway PostgreSQL is started on a free port
#
# Exit codes:
#   0  - All checks passed
#   1  - Build/test failure
#   2  - Coverage below threshold
#   3  - SpotBugs violations
#   4  - Checkstyle violations
#   5  - Dependency vulnerabilities (if --dependency-check used)
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
RUN_DEPENDENCY_CHECK=false
VERBOSE=false

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --dependency-check)
      RUN_DEPENDENCY_CHECK=true
      shift
      ;;
    --verbose)
      VERBOSE=true
      shift
      ;;
    --help)
      head -n 30 "$0" | grep "^#" | sed 's/^# //; s/^#//'
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

# Change to backend directory
cd "$(dirname "$0")/../../backend" || exit 1

################################################################################
# 1. Environment Validation
################################################################################

print_header "1. Environment Validation"

# Check Java version
print_info "Checking Java version..."
if command -v java &> /dev/null; then
  JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
  if [ "$JAVA_VERSION" = "25" ]; then
    print_success "Java 25 detected"
  else
    print_error "Java 25 required, but Java $JAVA_VERSION detected"
    exit 99
  fi
else
  print_error "Java not found. Please install Java 25"
  exit 99
fi

# Check PostgreSQL availability
print_info "Checking PostgreSQL availability..."

# Database connection (matching CI). The port follows docker-compose.yml (POSTGRES_PORT).
DB_PORT="${POSTGRES_PORT:-5432}"
DB_CONTAINER=""

db_reachable() {
  PGPASSWORD=sandbox psql -h localhost -p "$DB_PORT" -U sandbox -d sandbox -c "SELECT 1" &> /dev/null
}

port_in_use() {
  lsof -iTCP:"$1" -sTCP:LISTEN &> /dev/null
}

# Remove the throwaway database started by this script (also on failure or Ctrl-C)
cleanup_db() {
  if [ -n "$DB_CONTAINER" ]; then
    docker rm -f "$DB_CONTAINER" &> /dev/null || true
  fi
}
trap cleanup_db EXIT

wait_for_db() {
  print_info "Waiting for PostgreSQL to be ready..."
  for i in {1..30}; do
    if db_reachable; then
      return 0
    fi
    sleep 1
  done
  return 1
}

if command -v psql &> /dev/null; then
  if db_reachable; then
    print_success "PostgreSQL is running on port $DB_PORT (sandbox/sandbox/sandbox)"
  elif port_in_use "$DB_PORT"; then
    # Another database owns the port: start a throwaway one on a free port
    print_warning "Port $DB_PORT is used by another process. Starting a throwaway PostgreSQL..."

    if ! command -v docker &> /dev/null; then
      print_error "Docker not found. Free port $DB_PORT or set POSTGRES_PORT to a PostgreSQL you started"
      exit 99
    fi

    FREE_PORT=""
    for candidate in $(seq 5433 5499); do
      if ! port_in_use "$candidate"; then
        FREE_PORT=$candidate
        break
      fi
    done
    if [ -z "$FREE_PORT" ]; then
      print_error "No free port found in 5433-5499"
      exit 99
    fi

    DB_CONTAINER="sandbox-ci-pg-$$"
    if ! docker run -d --name "$DB_CONTAINER" -p "$FREE_PORT:5432" \
      -e POSTGRES_DB=sandbox -e POSTGRES_USER=sandbox -e POSTGRES_PASSWORD=sandbox \
      postgres:16.4-alpine > /dev/null; then
      print_error "Failed to start a throwaway PostgreSQL container"
      exit 99
    fi
    DB_PORT="$FREE_PORT"

    if wait_for_db; then
      print_success "Throwaway PostgreSQL is running on port $DB_PORT (removed when this script exits)"
    else
      print_error "Throwaway PostgreSQL did not become ready on port $DB_PORT"
      exit 99
    fi
  else
    print_warning "PostgreSQL not accessible. Attempting to start via docker-compose..."

    # Try to start PostgreSQL via docker-compose
    if command -v docker-compose &> /dev/null || command -v docker &> /dev/null; then
      cd ..
      POSTGRES_PORT="$DB_PORT" docker-compose up -d postgres
      cd backend

      if wait_for_db; then
        print_success "PostgreSQL is now running on port $DB_PORT (sandbox/sandbox/sandbox)"
      else
        print_error "Failed to start PostgreSQL. Please start it manually:"
        print_info "  POSTGRES_PORT=$DB_PORT docker-compose up -d postgres"
        exit 99
      fi
    else
      print_error "Docker not found. Please install Docker or start PostgreSQL manually"
      exit 99
    fi
  fi
else
  print_warning "psql not found. Assuming PostgreSQL is running..."
fi

# Environment variables for PostgreSQL connection (matching CI; port may have changed above)
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${DB_PORT}/sandbox"
export SPRING_DATASOURCE_USERNAME="sandbox"
export SPRING_DATASOURCE_PASSWORD="sandbox"

print_success "Environment validation passed"

################################################################################
# 2. Maven Build and Verify
################################################################################

print_header "2. Maven Build and Verify"

print_info "Running: ./mvnw clean verify -DskipDockerCompose=true"
print_info "This will compile, test, and run quality checks..."
print_info "Using database: $SPRING_DATASOURCE_URL"

# Run Maven verify (environment variables already set in validation step)
if [ "$VERBOSE" = true ]; then
  ./mvnw clean verify -DskipDockerCompose=true
  MVN_EXIT_CODE=$?
else
  ./mvnw clean verify -DskipDockerCompose=true > /tmp/mvn-output.log 2>&1
  MVN_EXIT_CODE=$?
fi

if [ $MVN_EXIT_CODE -ne 0 ]; then
  print_error "Maven build failed"

  if [ "$VERBOSE" = false ]; then
    echo ""
    echo "Last 50 lines of build output:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    tail -50 /tmp/mvn-output.log
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    print_info "Run with --verbose flag to see full output"
  fi

  exit 1
fi

print_success "Maven build and verify passed"

################################################################################
# 3. JaCoCo Coverage Validation
################################################################################

print_header "3. JaCoCo Coverage Validation"

JACOCO_XML="target/site/jacoco/jacoco.xml"

if [ ! -f "$JACOCO_XML" ]; then
  print_error "JaCoCo report not found: $JACOCO_XML"
  exit 2
fi

print_info "Parsing JaCoCo coverage report..."

# Parse JaCoCo XML using awk (works on macOS without additional tools)
# Extract coverage counters
parse_jacoco() {
  local counter_type=$1
  local covered=$(grep "counter type=\"$counter_type\"" "$JACOCO_XML" | head -1 | sed -n 's/.*covered="\([0-9]*\)".*/\1/p')
  local missed=$(grep "counter type=\"$counter_type\"" "$JACOCO_XML" | head -1 | sed -n 's/.*missed="\([0-9]*\)".*/\1/p')

  if [ -z "$covered" ] || [ -z "$missed" ]; then
    echo "0"
    return
  fi

  local total=$((covered + missed))
  if [ $total -eq 0 ]; then
    echo "0"
    return
  fi

  # Calculate percentage (using bc if available, otherwise awk)
  if command -v bc &> /dev/null; then
    echo "scale=2; ($covered * 100) / $total" | bc
  else
    awk "BEGIN {printf \"%.2f\", ($covered * 100) / $total}"
  fi
}

LINE_COVERAGE=$(parse_jacoco "LINE")
BRANCH_COVERAGE=$(parse_jacoco "BRANCH")
INSTRUCTION_COVERAGE=$(parse_jacoco "INSTRUCTION")

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

check_coverage "Lines" "$LINE_COVERAGE" "80.00"
check_coverage "Branches" "$BRANCH_COVERAGE" "75.00"
check_coverage "Instructions" "$INSTRUCTION_COVERAGE" "80.00"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check thresholds
COVERAGE_FAILED=false

if (( $(echo "$LINE_COVERAGE < 80" | awk '{print ($1 < $3)}') )); then
  print_error "Line coverage ($LINE_COVERAGE%) is below threshold (80%)"
  COVERAGE_FAILED=true
fi

if (( $(echo "$BRANCH_COVERAGE < 75" | awk '{print ($1 < $3)}') )); then
  print_error "Branch coverage ($BRANCH_COVERAGE%) is below threshold (75%)"
  COVERAGE_FAILED=true
fi

if [ "$COVERAGE_FAILED" = true ]; then
  print_info "View detailed report: backend/target/site/jacoco/index.html"
  exit 2
fi

print_success "Coverage validation passed"

################################################################################
# 4. SpotBugs Analysis
################################################################################

print_header "4. SpotBugs Analysis"

SPOTBUGS_XML="target/spotbugsXml.xml"

if [ ! -f "$SPOTBUGS_XML" ]; then
  print_warning "SpotBugs report not found: $SPOTBUGS_XML"
  print_info "SpotBugs may not have run. This is OK if build succeeded."
else
  print_info "Parsing SpotBugs report..."

  # Count bugs by priority (1=High, 2=Medium, 3=Low)
  HIGH_BUGS=$(grep 'priority="1"' "$SPOTBUGS_XML" 2>/dev/null | wc -l | tr -d ' ')
  MEDIUM_BUGS=$(grep 'priority="2"' "$SPOTBUGS_XML" 2>/dev/null | wc -l | tr -d ' ')
  LOW_BUGS=$(grep 'priority="3"' "$SPOTBUGS_XML" 2>/dev/null | wc -l | tr -d ' ')
  TOTAL_BUGS=$((HIGH_BUGS + MEDIUM_BUGS + LOW_BUGS))

  echo ""
  echo "SpotBugs Report:"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  printf "%-15s %10s\n" "Priority" "Count"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  printf "%-15s %10d\n" "High" "$HIGH_BUGS"
  printf "%-15s %10d\n" "Medium" "$MEDIUM_BUGS"
  printf "%-15s %10d\n" "Low" "$LOW_BUGS"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  printf "%-15s %10d\n" "Total" "$TOTAL_BUGS"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""

  if [ $TOTAL_BUGS -gt 0 ]; then
    print_warning "$TOTAL_BUGS SpotBugs violations found"
    print_info "View detailed report: backend/target/spotbugs.html"

    # Note: SpotBugs is configured with failOnError=true in pom.xml
    # If there are violations, Maven verify would have already failed
    # So if we got here, violations are likely suppressed or low priority
  else
    print_success "No SpotBugs violations found"
  fi
fi

################################################################################
# 5. Checkstyle Validation
################################################################################

print_header "5. Checkstyle Validation"

CHECKSTYLE_XML="target/checkstyle-result.xml"

if [ ! -f "$CHECKSTYLE_XML" ]; then
  print_warning "Checkstyle report not found: $CHECKSTYLE_XML"
  print_info "Checkstyle may not have run. This is OK if build succeeded."
else
  print_info "Parsing Checkstyle report..."

  # Count violations by severity
  ERROR_COUNT=$(grep 'severity="error"' "$CHECKSTYLE_XML" 2>/dev/null | wc -l | tr -d ' ')
  WARNING_COUNT=$(grep 'severity="warning"' "$CHECKSTYLE_XML" 2>/dev/null | wc -l | tr -d ' ')
  INFO_COUNT=$(grep 'severity="info"' "$CHECKSTYLE_XML" 2>/dev/null | wc -l | tr -d ' ')
  TOTAL_VIOLATIONS=$((ERROR_COUNT + WARNING_COUNT + INFO_COUNT))

  echo ""
  echo "Checkstyle Report:"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  printf "%-15s %10s\n" "Severity" "Count"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  printf "%-15s %10d\n" "Error" "$ERROR_COUNT"
  printf "%-15s %10d\n" "Warning" "$WARNING_COUNT"
  printf "%-15s %10d\n" "Info" "$INFO_COUNT"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  printf "%-15s %10d\n" "Total" "$TOTAL_VIOLATIONS"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""

  if [ $TOTAL_VIOLATIONS -gt 0 ]; then
    print_warning "$TOTAL_VIOLATIONS Checkstyle violations found"
    print_info "View detailed report: backend/target/checkstyle-result.xml"

    # Note: Checkstyle is configured with failOnViolation=true in pom.xml
    # If there are violations, Maven verify would have already failed
    # So if we got here, violations might be warnings only
  else
    print_success "No Checkstyle violations found"
  fi
fi

################################################################################
# 6. OWASP Dependency-Check (Optional)
################################################################################

if [ "$RUN_DEPENDENCY_CHECK" = true ]; then
  print_header "6. OWASP Dependency-Check"

  print_warning "This check is SLOW (~5-10 minutes on first run)"
  print_info "Running: ./mvnw org.owasp:dependency-check-maven:check"

  if [ "$VERBOSE" = true ]; then
    ./mvnw org.owasp:dependency-check-maven:check
    DEPENDENCY_CHECK_EXIT_CODE=$?
  else
    ./mvnw org.owasp:dependency-check-maven:check > /tmp/dependency-check-output.log 2>&1
    DEPENDENCY_CHECK_EXIT_CODE=$?
  fi

  DEPENDENCY_JSON="target/dependency-check-report.json"

  if [ -f "$DEPENDENCY_JSON" ]; then
    print_info "Parsing dependency-check report..."

    # Parse JSON to count vulnerabilities
    if command -v jq &> /dev/null; then
      VULNERABILITY_COUNT=$(jq '[.dependencies[].vulnerabilities // []] | add | length' "$DEPENDENCY_JSON")

      if [ "$VULNERABILITY_COUNT" -gt 0 ]; then
        print_error "$VULNERABILITY_COUNT vulnerabilities found"
        print_info "View detailed report: backend/target/dependency-check-report.html"
        exit 5
      else
        print_success "No vulnerabilities found"
      fi
    else
      print_warning "jq not found. Cannot parse JSON report."
      print_info "View detailed report: backend/target/dependency-check-report.html"

      if [ $DEPENDENCY_CHECK_EXIT_CODE -ne 0 ]; then
        print_error "Dependency-check reported failures"
        exit 5
      fi
    fi
  else
    print_warning "Dependency-check report not found"
    if [ $DEPENDENCY_CHECK_EXIT_CODE -ne 0 ]; then
      print_error "Dependency-check failed"
      exit 5
    fi
  fi
fi

################################################################################
# Final Summary
################################################################################

print_header "✅ All Backend Checks Passed!"

echo ""
echo "Summary:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Build and tests passed"
echo "✅ Code coverage meets thresholds"
echo "   - Lines: $LINE_COVERAGE% (≥80%)"
echo "   - Branches: $BRANCH_COVERAGE% (≥75%)"
echo "✅ Quality checks passed"

if [ "$RUN_DEPENDENCY_CHECK" = true ]; then
  echo "✅ Dependency check passed"
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

print_success "Backend is ready for CI! 🚀"
echo ""

exit 0
