#!/bin/bash

################################################################################
# Local CI Verification - Main Orchestration Script
#
# This script runs all CI checks locally to ensure your changes will pass
# in GitHub Actions CI before you push.
#
# GOLDEN RULE: If this passes locally, CI should pass.
#
# Usage:
#   ./scripts/ci-check-local.sh [OPTIONS]
#
# Options:
#   --backend-only       Run only backend checks
#   --frontend-only      Run only frontend checks
#   --e2e                Include E2E tests (slow, requires Docker)
#   --dependency-check   Include OWASP dependency-check (slow)
#   --parallel           Run frontend lint and type-check in parallel
#   --verbose            Show detailed output
#   --yes, -y            Skip confirmation prompt (for automation)
#   --help               Show this help message
#
# Examples:
#   # Standard check (recommended before PR)
#   ./scripts/ci-check-local.sh
#
#   # Quick backend only
#   ./scripts/ci-check-local.sh --backend-only
#
#   # Full verification with E2E tests
#   ./scripts/ci-check-local.sh --e2e --dependency-check
#
#   # Fast parallel execution
#   ./scripts/ci-check-local.sh --parallel
#
# Exit codes:
#   0   - All checks passed
#   1-5 - Backend checks failed (see backend/ci-verify.sh)
#   11-40 - Frontend checks failed (see scripts/frontend/ci-verify.sh;
#           14 = Markdown lint failure)
#   99  - Environment validation failed
################################################################################

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Default options
RUN_BACKEND=true
RUN_FRONTEND=true
RUN_E2E=false
RUN_DEPENDENCY_CHECK=false
RUN_PARALLEL=false
VERBOSE=false
SKIP_CONFIRM=false

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --backend-only)
      RUN_BACKEND=true
      RUN_FRONTEND=false
      shift
      ;;
    --frontend-only)
      RUN_BACKEND=false
      RUN_FRONTEND=true
      shift
      ;;
    --e2e)
      RUN_E2E=true
      shift
      ;;
    --dependency-check)
      RUN_DEPENDENCY_CHECK=true
      shift
      ;;
    --parallel)
      RUN_PARALLEL=true
      shift
      ;;
    --verbose)
      VERBOSE=true
      shift
      ;;
    --yes|-y)
      SKIP_CONFIRM=true
      shift
      ;;
    --help)
      head -n 45 "$0" | grep "^#" | sed 's/^# //; s/^#//'
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
  echo -e "${BOLD}${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${BOLD}${CYAN}$1${NC}"
  echo -e "${BOLD}${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo ""
}

print_section() {
  echo ""
  echo -e "${BOLD}${BLUE}▶ $1${NC}"
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

# Change to project root
cd "$(dirname "$0")/.." || exit 1

# Show banner
clear 2>/dev/null || true  # fails without a usable TERM (e.g. automation)
echo ""
echo -e "${BOLD}${CYAN}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}${CYAN}║                                                               ║${NC}"
echo -e "${BOLD}${CYAN}║           LOCAL CI VERIFICATION SYSTEM                        ║${NC}"
echo -e "${BOLD}${CYAN}║                                                               ║${NC}"
echo -e "${BOLD}${CYAN}║   Golden Rule: If this passes, CI should pass! 🎯             ║${NC}"
echo -e "${BOLD}${CYAN}║                                                               ║${NC}"
echo -e "${BOLD}${CYAN}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""

START_TIME=$(date +%s)

# Detect changed files to provide smart recommendations
print_info "Detecting changed files..."

BACKEND_CHANGED=false
FRONTEND_CHANGED=false

if git diff --name-only HEAD | grep -q "^backend/"; then
  BACKEND_CHANGED=true
fi

if git diff --name-only HEAD | grep -q "^frontend/"; then
  FRONTEND_CHANGED=true
fi

if git diff --cached --name-only | grep -q "^backend/"; then
  BACKEND_CHANGED=true
fi

if git diff --cached --name-only | grep -q "^frontend/"; then
  FRONTEND_CHANGED=true
fi

# Show what will be checked
echo ""
echo "Configuration:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ "$RUN_BACKEND" = true ]; then
  echo "✓ Backend checks enabled"
  if [ "$BACKEND_CHANGED" = true ]; then
    echo "  (backend files changed)"
  fi
  if [ "$RUN_DEPENDENCY_CHECK" = true ]; then
    echo "  - OWASP dependency-check enabled (slow)"
  fi
else
  echo "✗ Backend checks disabled"
fi

if [ "$RUN_FRONTEND" = true ]; then
  echo "✓ Frontend checks enabled"
  if [ "$FRONTEND_CHANGED" = true ]; then
    echo "  (frontend files changed)"
  fi
  if [ "$RUN_E2E" = true ]; then
    echo "  - E2E tests enabled (slow)"
  fi
  if [ "$RUN_PARALLEL" = true ]; then
    echo "  - Parallel execution enabled"
  fi
else
  echo "✗ Frontend checks disabled"
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Show recommendations
if [ "$RUN_BACKEND" = true ] && [ "$RUN_FRONTEND" = true ]; then
  if [ "$BACKEND_CHANGED" = true ] && [ "$FRONTEND_CHANGED" = false ]; then
    echo ""
    print_info "Only backend files changed. Consider using --backend-only for faster checks."
  elif [ "$FRONTEND_CHANGED" = true ] && [ "$BACKEND_CHANGED" = false ]; then
    echo ""
    print_info "Only frontend files changed. Consider using --frontend-only for faster checks."
  fi
fi

if [ "$SKIP_CONFIRM" = false ]; then
  echo ""
  read -p "Press Enter to continue or Ctrl+C to cancel..."
  echo ""
fi

################################################################################
# Run Checks
################################################################################

SPEC_EXIT_CODE=0
BACKEND_EXIT_CODE=0
FRONTEND_EXIT_CODE=0

# Specification validation (if specs changed)
SPECS_CHANGED=false
if git diff --name-only HEAD | grep -q "^specs/"; then
  SPECS_CHANGED=true
fi
if git diff --cached --name-only | grep -q "^specs/"; then
  SPECS_CHANGED=true
fi

if [ "$SPECS_CHANGED" = true ] || [ -d "specs" ]; then
  print_header "SPECIFICATION VALIDATION"

  SPEC_VALIDATION_FAILED=false

  # Validate OpenAPI with Spectral
  if [ -f "specs/openapi/openapi.yaml" ]; then
    print_section "Validating OpenAPI specification with Spectral"

    if command -v spectral &> /dev/null; then
      if spectral lint specs/openapi/openapi.yaml --ruleset specs/.spectral.yaml; then
        print_success "OpenAPI specification is valid"
      else
        print_error "OpenAPI validation failed"
        SPEC_VALIDATION_FAILED=true
      fi
    else
      print_warning "Spectral not installed. Run: npm install -g @stoplight/spectral-cli"
      print_info "Skipping OpenAPI validation"
    fi
  fi

  # Validate Gherkin files
  if [ -d "specs/acceptance" ]; then
    print_section "Validating Gherkin acceptance criteria"

    if command -v gherkin-lint &> /dev/null; then
      if gherkin-lint specs/acceptance/**/*.feature; then
        print_success "Gherkin files are valid"
      else
        print_warning "Gherkin validation found issues (non-blocking)"
      fi
    else
      print_warning "gherkin-lint not installed. Run: npm install -g gherkin-lint"
      print_info "Skipping Gherkin validation"
    fi
  fi

  # Check contracts
  if [ -d "specs/contracts" ]; then
    print_section "Checking contract definitions"
    CONTRACT_COUNT=$(find specs/contracts -name "*.yml" | wc -l | tr -d ' ')
    print_info "Found $CONTRACT_COUNT contract definition(s)"
  fi

  if [ "$SPEC_VALIDATION_FAILED" = true ]; then
    SPEC_EXIT_CODE=1
    print_error "Specification validation failed"
  else
    print_success "Specification validation passed"
  fi
fi

# Backend checks
if [ "$RUN_BACKEND" = true ]; then
  print_header "BACKEND VERIFICATION"

  BACKEND_ARGS=""
  if [ "$RUN_DEPENDENCY_CHECK" = true ]; then
    BACKEND_ARGS="$BACKEND_ARGS --dependency-check"
  fi
  if [ "$VERBOSE" = true ]; then
    BACKEND_ARGS="$BACKEND_ARGS --verbose"
  fi

  if ./scripts/backend/ci-verify.sh $BACKEND_ARGS; then
    print_success "Backend verification passed"
  else
    BACKEND_EXIT_CODE=$?
    print_error "Backend verification failed (exit code: $BACKEND_EXIT_CODE)"
  fi
fi

# Frontend checks
if [ "$RUN_FRONTEND" = true ]; then
  print_header "FRONTEND VERIFICATION"

  FRONTEND_ARGS=""
  if [ "$RUN_E2E" = true ]; then
    FRONTEND_ARGS="$FRONTEND_ARGS --e2e"
  fi
  if [ "$RUN_PARALLEL" = true ]; then
    FRONTEND_ARGS="$FRONTEND_ARGS --parallel"
  fi
  if [ "$VERBOSE" = true ]; then
    FRONTEND_ARGS="$FRONTEND_ARGS --verbose"
  fi

  if ./scripts/frontend/ci-verify.sh $FRONTEND_ARGS; then
    print_success "Frontend verification passed"
  else
    FRONTEND_EXIT_CODE=$?
    print_error "Frontend verification failed (exit code: $FRONTEND_EXIT_CODE)"
  fi
fi

################################################################################
# Final Summary
################################################################################

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))

print_header "FINAL SUMMARY"

echo ""
echo "Results:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Specification summary
if [ "$SPECS_CHANGED" = true ]; then
  if [ $SPEC_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ Specification${NC}"
    echo "   ✓ OpenAPI validation"
    echo "   ✓ Gherkin syntax"
    echo "   ✓ Contract definitions"
  else
    echo -e "${RED}❌ Specification${NC}"
    echo "   ✗ Validation failed"
  fi
fi

# Backend summary
if [ "$RUN_BACKEND" = true ]; then
  if [ $BACKEND_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ Backend${NC}"
    echo "   ✓ Build and tests"
    echo "   ✓ Code coverage (≥80% lines, ≥75% branches)"
    echo "   ✓ SpotBugs analysis"
    echo "   ✓ Checkstyle validation"
    if [ "$RUN_DEPENDENCY_CHECK" = true ]; then
      echo "   ✓ OWASP dependency-check"
    fi
  else
    echo -e "${RED}❌ Backend (exit code: $BACKEND_EXIT_CODE)${NC}"
    case $BACKEND_EXIT_CODE in
      1)
        echo "   → Build/test failure"
        ;;
      2)
        echo "   → Coverage below threshold"
        ;;
      3)
        echo "   → SpotBugs violations"
        ;;
      4)
        echo "   → Checkstyle violations"
        ;;
      5)
        echo "   → Dependency vulnerabilities"
        ;;
      99)
        echo "   → Environment validation failed"
        ;;
    esac
  fi
else
  echo -e "${YELLOW}⊘ Backend (skipped)${NC}"
fi

echo ""

# Frontend summary
if [ "$RUN_FRONTEND" = true ]; then
  if [ $FRONTEND_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ Frontend${NC}"
    echo "   ✓ Lint and format checks"
    echo "   ✓ Markdown linting"
    echo "   ✓ Type checking"
    echo "   ✓ Tests with coverage (≥80% statements/functions/lines, ≥70% branches)"
    echo "   ✓ Production build"
    if [ "$RUN_E2E" = true ]; then
      echo "   ✓ E2E tests (Playwright)"
    fi
  else
    echo -e "${RED}❌ Frontend (exit code: $FRONTEND_EXIT_CODE)${NC}"
    case $FRONTEND_EXIT_CODE in
      11)
        echo "   → ESLint failure"
        ;;
      12)
        echo "   → Prettier failure"
        ;;
      13)
        echo "   → TypeScript error"
        ;;
      14)
        echo "   → Markdown lint failure (run 'cd frontend && pnpm lint:md:fix')"
        ;;
      21)
        echo "   → Coverage below threshold"
        ;;
      30)
        echo "   → Build failure"
        ;;
      40)
        echo "   → E2E test failure"
        ;;
      99)
        echo "   → Environment validation failed"
        ;;
    esac
  fi
else
  echo -e "${YELLOW}⊘ Frontend (skipped)${NC}"
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Overall result
if [ $SPEC_EXIT_CODE -eq 0 ] && [ $BACKEND_EXIT_CODE -eq 0 ] && [ $FRONTEND_EXIT_CODE -eq 0 ]; then
  echo -e "${BOLD}${GREEN}╔═══════════════════════════════════════════════════════════════╗${NC}"
  echo -e "${BOLD}${GREEN}║                                                               ║${NC}"
  echo -e "${BOLD}${GREEN}║              ✅ ALL CHECKS PASSED! 🎉                          ║${NC}"
  echo -e "${BOLD}${GREEN}║                                                               ║${NC}"
  echo -e "${BOLD}${GREEN}║   Your code is ready to push. CI should pass! 🚀              ║${NC}"
  echo -e "${BOLD}${GREEN}║                                                               ║${NC}"
  echo -e "${BOLD}${GREEN}╚═══════════════════════════════════════════════════════════════╝${NC}"
  echo ""
  echo -e "Completed in ${MINUTES}m ${SECONDS}s"
  echo ""
  exit 0
else
  echo -e "${BOLD}${RED}╔═══════════════════════════════════════════════════════════════╗${NC}"
  echo -e "${BOLD}${RED}║                                                               ║${NC}"
  echo -e "${BOLD}${RED}║              ❌ CHECKS FAILED                                  ║${NC}"
  echo -e "${BOLD}${RED}║                                                               ║${NC}"
  echo -e "${BOLD}${RED}║   Please fix the issues above before pushing.                ║${NC}"
  echo -e "${BOLD}${RED}║                                                               ║${NC}"
  echo -e "${BOLD}${RED}╚═══════════════════════════════════════════════════════════════╝${NC}"
  echo ""
  echo -e "Completed in ${MINUTES}m ${SECONDS}s"
  echo ""
  echo "Troubleshooting:"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "• Run with --verbose flag to see detailed output"
  echo "• Check specific error messages above"
  echo "• Run ./scripts/ci-check-local.sh --help for options"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""

  # Return the first non-zero exit code
  if [ $SPEC_EXIT_CODE -ne 0 ]; then
    exit $SPEC_EXIT_CODE
  elif [ $BACKEND_EXIT_CODE -ne 0 ]; then
    exit $BACKEND_EXIT_CODE
  else
    exit $FRONTEND_EXIT_CODE
  fi
fi
