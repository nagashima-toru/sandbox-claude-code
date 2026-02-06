#!/bin/bash

################################################################################
# Orval API Client Generator
#
# Generate TypeScript API client from OpenAPI specification using Orval.
# This script runs the code generation process in the frontend directory.
#
# Usage:
#   ./scripts/generate-api.sh [OPTIONS]
#
# Options:
#   --help               Show this help message
#
# Prerequisites:
#   1. Backend OpenAPI specification must be generated first:
#      cd backend && ./mvnw verify
#      (Creates backend/target/openapi/openapi.yaml)
#
#   2. pnpm must be installed in frontend directory
#
# What it generates:
#   - frontend/src/lib/api/generated/messages.ts
#     (Types, API functions, React Query hooks)
#
# Examples:
#   # Generate API client
#   ./scripts/generate-api.sh
#
#   # Full workflow (backend + frontend)
#   cd backend && ./mvnw verify && cd .. && ./scripts/generate-api.sh
#
# Exit codes:
#   0   - Successfully generated
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

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
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

# Check if package.json has generate:api script
if [ ! -f "frontend/package.json" ]; then
  print_error "frontend/package.json not found"
  exit 1
fi

if ! grep -q '"generate:api"' frontend/package.json; then
  print_error "generate:api script not found in frontend/package.json"
  exit 1
fi

print_success "Environment validation passed"

# Show recommendation about backend OpenAPI generation
echo ""
print_warning "Prerequisite: Backend OpenAPI Specification"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Before running this script, ensure the backend OpenAPI spec"
echo "  is up-to-date by running:"
echo ""
echo "    cd backend && ./mvnw verify"
echo ""
echo "  This creates: backend/target/openapi/openapi.yaml"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if backend OpenAPI spec exists (warn if not)
if [ ! -f "backend/target/openapi/openapi.yaml" ]; then
  print_warning "Backend OpenAPI spec not found at backend/target/openapi/openapi.yaml"
  echo ""
  read -p "Continue anyway? (y/N): " -n 1 -r
  echo ""
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_info "Aborted"
    exit 0
  fi
fi

# Execute generation
print_info "Generating TypeScript API client from OpenAPI specification..."
echo ""

cd frontend || exit 1

if pnpm generate:api; then
  cd .. || exit 1

  echo ""
  print_success "API client generated successfully"

  # Check if generated files exist
  if [ -f "frontend/src/lib/api/generated/messages.ts" ]; then
    echo ""
    echo "Generated files:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  ✓ frontend/src/lib/api/generated/messages.ts"
    echo "    (Types, API functions, React Query hooks)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""

    # Show file size
    FILE_SIZE=$(wc -c < "frontend/src/lib/api/generated/messages.ts" | tr -d ' ')
    LINES=$(wc -l < "frontend/src/lib/api/generated/messages.ts" | tr -d ' ')
    print_info "Generated ${LINES} lines (${FILE_SIZE} bytes)"

    echo ""
    print_info "Usage example:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  import { useGetAllMessages } from '@/lib/api/generated/messages';"
    echo ""
    echo "  function MyComponent() {"
    echo "    const { data, isLoading } = useGetAllMessages();"
    echo "    // ..."
    echo "  }"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
  else
    print_warning "Generated file not found at expected location"
    echo "Check frontend/src/lib/api/generated/ for generated files"
    echo ""
  fi

  exit 0
else
  EXIT_CODE=$?
  cd .. || exit 1

  echo ""
  print_error "API client generation failed (exit code: $EXIT_CODE)"
  echo ""
  echo "Troubleshooting:"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "  1. Ensure backend OpenAPI spec exists:"
  echo "     cd backend && ./mvnw verify"
  echo ""
  echo "  2. Check Orval configuration:"
  echo "     frontend/orval.config.ts"
  echo ""
  echo "  3. Verify OpenAPI spec is valid:"
  echo "     cat backend/target/openapi/openapi.yaml"
  echo ""
  echo "  4. See docs/frontend/ORVAL_API_GENERATION.md for details"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""

  exit 1
fi
