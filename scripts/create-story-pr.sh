#!/bin/bash
# Story PR 作成スクリプト（テンプレート自動選択）
#
# Usage: ./scripts/create-story-pr.sh [issue-number] [story-number]
# Example: ./scripts/create-story-pr.sh 133 5

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
error() {
  echo -e "${RED}Error: $1${NC}" >&2
  exit 1
}

info() {
  echo -e "${BLUE}$1${NC}"
}

success() {
  echo -e "${GREEN}$1${NC}"
}

warning() {
  echo -e "${YELLOW}$1${NC}"
}

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
  error "gh CLI is not installed. Please install it: brew install gh"
fi

# Parse arguments
if [ $# -eq 0 ]; then
  # Interactive mode
  info "Interactive mode: Creating Story PR"
  echo ""

  # Get issue number
  read -p "Enter Issue number: " ISSUE_NUM
  if [ -z "$ISSUE_NUM" ]; then
    error "Issue number is required"
  fi

  # Get story number
  read -p "Enter Story number: " STORY_NUM
  if [ -z "$STORY_NUM" ]; then
    error "Story number is required"
  fi
elif [ $# -eq 2 ]; then
  # Command line arguments
  ISSUE_NUM=$1
  STORY_NUM=$2
else
  error "Usage: $0 [issue-number] [story-number]"
fi

info "Creating PR for Issue #${ISSUE_NUM}, Story ${STORY_NUM}..."
echo ""

# Find Epic base branch
info "Looking for Epic base branch..."
EPIC_BASE=$(git branch -r | grep "origin/feature/issue-${ISSUE_NUM}-" | grep -v story | sed 's/origin\///' | sed 's/^[[:space:]]*//' | head -1)

if [ -z "$EPIC_BASE" ]; then
  error "Epic base branch not found for issue #${ISSUE_NUM}

Expected branch name pattern: feature/issue-${ISSUE_NUM}-[epic-name]

Please ensure the Epic base branch exists on remote."
fi

success "✓ Found Epic base branch: ${EPIC_BASE}"

# Get current branch (Story branch)
STORY_BRANCH=$(git branch --show-current)
info "Current branch (Story): ${STORY_BRANCH}"

# Validate Story branch name
if [[ ! "$STORY_BRANCH" =~ story[0-9]+ ]]; then
  warning "Current branch name doesn't contain 'story[N]': ${STORY_BRANCH}"
  read -p "Continue anyway? (y/N): " CONFIRM
  if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    error "Aborted by user"
  fi
fi

# Check if PR template exists
TEMPLATE_PATH=".github/PULL_REQUEST_TEMPLATE/story.md"
if [ ! -f "$TEMPLATE_PATH" ]; then
  error "PR template not found: ${TEMPLATE_PATH}"
fi

success "✓ PR template found: ${TEMPLATE_PATH}"
echo ""

# Display PR info
info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
info "PR Configuration:"
info "  Base:     ${EPIC_BASE}"
info "  Head:     ${STORY_BRANCH}"
info "  Template: ${TEMPLATE_PATH}"
info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Confirm
read -p "Create PR? (Y/n): " CONFIRM
if [[ "$CONFIRM" =~ ^[Nn]$ ]]; then
  warning "Aborted by user"
  exit 0
fi

# Create PR
info "Creating PR with template..."
PR_URL=$(gh pr create \
  --base "$EPIC_BASE" \
  --head "$STORY_BRANCH" \
  --template "$TEMPLATE_PATH")

if [ $? -eq 0 ]; then
  echo ""
  success "✓ PR created successfully!"
  success "  URL: ${PR_URL}"
  echo ""
  info "Next steps:"
  info "  1. Fill in the PR template"
  info "  2. Wait for CI checks to pass"
  info "  3. Request review"
else
  error "Failed to create PR"
fi
