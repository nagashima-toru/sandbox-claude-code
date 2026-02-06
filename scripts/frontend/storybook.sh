#!/bin/bash

################################################################################
# Storybook Development Server Launcher
#
# Start the Storybook development server for component development and testing.
# Storybook provides an isolated environment for developing and testing UI
# components.
#
# Usage:
#   ./scripts/storybook.sh [OPTIONS]
#
# Options:
#   --build              Build static Storybook instead of starting dev server
#   --help               Show this help message
#
# Examples:
#   # Start Storybook dev server (default)
#   ./scripts/storybook.sh
#
#   # Build static Storybook
#   ./scripts/storybook.sh --build
#
# Features:
#   - Component isolation
#   - Interactive controls
#   - Accessibility testing
#   - Responsive viewport testing
#   - MSW (Mock Service Worker) for API mocking
#
# Exit codes:
#   0   - Successfully started/built
#   1   - Error occurred
################################################################################

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Helper functions
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }

# Default options
BUILD_MODE=false

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --build)
      BUILD_MODE=true
      shift
      ;;
    --help)
      head -n 37 "$0" | grep "^#" | sed 's/^# //; s/^#//'
      exit 0
      ;;
    *)
      print_error "Unknown option: $1"
      echo "Use --help for usage information"
      exit 1
      ;;
  esac
done

# Move to project root
cd "$(dirname "$0")/.." || exit 1

# Validation
print_info "Validating environment..."

# Check if frontend directory exists
if [ ! -d "frontend" ]; then
  print_error "frontend/ directory not found"
  echo "Are you in the project root directory?"
  exit 1
fi

# Check if pnpm is available
if ! command -v pnpm &> /dev/null; then
  print_error "pnpm is not installed"
  echo "Please install pnpm: https://pnpm.io/installation"
  exit 1
fi

# Check if package.json has storybook script
if [ ! -f "frontend/package.json" ]; then
  print_error "frontend/package.json not found"
  exit 1
fi

if [ "$BUILD_MODE" = true ]; then
  if ! grep -q '"build-storybook"' frontend/package.json; then
    print_error "build-storybook script not found in frontend/package.json"
    exit 1
  fi
else
  if ! grep -q '"storybook"' frontend/package.json; then
    print_error "storybook script not found in frontend/package.json"
    exit 1
  fi
fi

print_success "Environment validation passed"

# Execute Storybook
cd frontend || exit 1

if [ "$BUILD_MODE" = true ]; then
  # Build static Storybook
  print_info "Building static Storybook..."
  echo ""

  if pnpm build-storybook; then
    cd .. || exit 1

    echo ""
    print_success "Storybook built successfully"
    echo ""
    echo "Output directory:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  frontend/storybook-static/"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    print_info "Serve locally: cd frontend/storybook-static && python3 -m http.server 6006"
    echo ""

    exit 0
  else
    EXIT_CODE=$?
    cd .. || exit 1

    echo ""
    print_error "Storybook build failed (exit code: $EXIT_CODE)"
    exit 1
  fi
else
  # Start Storybook dev server
  echo ""
  print_info "Starting Storybook development server..."
  echo ""
  echo "Features:"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "  ✓ Component isolation"
  echo "  ✓ Interactive controls"
  echo "  ✓ Accessibility testing (a11y addon)"
  echo "  ✓ Responsive viewport testing"
  echo "  ✓ MSW (Mock Service Worker) for API mocking"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""

  if pnpm storybook; then
    cd .. || exit 1
    exit 0
  else
    EXIT_CODE=$?
    cd .. || exit 1

    echo ""
    print_error "Storybook failed to start (exit code: $EXIT_CODE)"
    echo ""
    echo "Troubleshooting:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  1. Ensure dependencies are installed:"
    echo "     cd frontend && pnpm install"
    echo ""
    echo "  2. Check port 6006 is not in use:"
    echo "     lsof -i :6006"
    echo ""
    echo "  3. Check Storybook configuration:"
    echo "     frontend/.storybook/"
    echo ""
    echo "  4. See docs/frontend/STORYBOOK.md for details"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""

    exit 1
  fi
fi
