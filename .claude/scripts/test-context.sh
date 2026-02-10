#!/bin/bash
# Test Context and Hooks related tests
#
# This script runs unit tests for Context and Hooks to quickly verify
# authentication-related functionality without running the full test suite.
#
# Usage:
#   ./.claude/scripts/test-context.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$PROJECT_ROOT/frontend"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Running Context and Hooks tests..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Run Context tests
echo ""
echo "📦 Testing useAuthContext..."
pnpm test tests/unit/hooks/useAuthContext.test.tsx

# Run Permission tests
echo ""
echo "🔒 Testing usePermission..."
pnpm test tests/unit/hooks/usePermission.test.tsx

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ All Context/Hooks tests passed"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
