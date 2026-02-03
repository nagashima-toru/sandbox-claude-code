#!/bin/bash

# Format code after Claude Code edits files
# This script is called by Claude Code hooks (tool:Edit:after, tool:Write:after)

set -e

# Get the absolute path to the project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Parse arguments
MODIFIED_FILE="$1"

if [ -z "$MODIFIED_FILE" ]; then
  echo "Usage: $0 <modified_file_path>"
  exit 1
fi

# Convert to absolute path if relative
if [[ ! "$MODIFIED_FILE" = /* ]]; then
  MODIFIED_FILE="$PROJECT_ROOT/$MODIFIED_FILE"
fi

# Check if file exists
if [ ! -f "$MODIFIED_FILE" ]; then
  echo "File not found: $MODIFIED_FILE"
  exit 1
fi

# Determine which formatter to use based on file location and extension
if [[ "$MODIFIED_FILE" == */backend/* ]]; then
  # Backend: Java files - use Spotless
  if [[ "$MODIFIED_FILE" == *.java ]]; then
    echo "Formatting Java file: $MODIFIED_FILE"
    cd "$PROJECT_ROOT/backend"
    ./mvnw spotless:apply -q 2>/dev/null || true
  fi
elif [[ "$MODIFIED_FILE" == */frontend/* ]]; then
  # Frontend: TypeScript/JavaScript files - use Prettier
  if [[ "$MODIFIED_FILE" == *.ts || "$MODIFIED_FILE" == *.tsx || "$MODIFIED_FILE" == *.js || "$MODIFIED_FILE" == *.jsx ]]; then
    echo "Formatting frontend file: $MODIFIED_FILE"
    cd "$PROJECT_ROOT/frontend"
    pnpm prettier --write "$MODIFIED_FILE" --log-level silent 2>/dev/null || true
  fi
fi

exit 0