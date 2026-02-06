#!/bin/bash

################################################################################
# Docker Production Test Environment Launcher
#
# Start the Docker production test environment.
# This script launches docker compose using ONLY docker-compose.yml, excluding
# docker-compose.override.yml (development mode overrides).
#
# Usage:
#   ./scripts/docker-prod.sh [OPTIONS]
#
# Options:
#   -d, --detach         Run containers in background (detached mode)
#   --build              Force rebuild of images before starting
#   --help               Show this help message
#
# Examples:
#   # Start in foreground (logs visible)
#   ./scripts/docker-prod.sh
#
#   # Start in background
#   ./scripts/docker-prod.sh -d
#
#   # Rebuild and start
#   ./scripts/docker-prod.sh --build
#
# Important:
#   - This mode does NOT use docker-compose.override.yml
#   - No volume mounts (source code is copied into images)
#   - No hot reload
#   - Uses production builds
#   - Accessed via Nginx reverse proxy
#
# Exit codes:
#   0   - Successfully started
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
DETACH_MODE=false
BUILD_MODE=false

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -d|--detach)
      DETACH_MODE=true
      shift
      ;;
    --build)
      BUILD_MODE=true
      shift
      ;;
    --help)
      head -n 45 "$0" | grep "^#" | sed 's/^# //; s/^#//'
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

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
  print_error "docker-compose.yml not found"
  echo "Are you in the project root directory?"
  exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
  print_error "Docker is not installed"
  echo "Please install Docker: https://docs.docker.com/get-docker/"
  exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
  print_error "Docker daemon is not running"
  echo "Please start Docker Desktop or Docker daemon"
  exit 1
fi

print_success "Environment validation passed"

# Warning about production mode
echo ""
print_warning "Production Test Mode"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  - docker-compose.override.yml is DISABLED"
echo "  - Source code changes require rebuild"
echo "  - No hot reload"
echo "  - Production build will be used"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Build Docker command (explicitly use only docker-compose.yml)
DOCKER_CMD="docker compose -f docker-compose.yml up"

if [ "$BUILD_MODE" = true ]; then
  DOCKER_CMD="$DOCKER_CMD --build"
fi

if [ "$DETACH_MODE" = true ]; then
  DOCKER_CMD="$DOCKER_CMD -d"
fi

# Show what will be executed
print_info "Starting Docker production test environment..."
print_info "Command: $DOCKER_CMD"
echo ""

# Execute docker compose
$DOCKER_CMD

# Show URLs after successful start
if [ "$DETACH_MODE" = true ]; then
  echo ""
  print_success "Docker containers started in background"
  echo ""
  echo "Access your application:"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "  Application: http://localhost"
  echo "    → Frontend: http://localhost/"
  echo "    → Backend:  http://localhost/api/"
  echo "    (via Nginx reverse proxy)"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""
  print_info "View logs: docker compose -f docker-compose.yml logs -f"
  print_info "Stop:      docker compose -f docker-compose.yml down"
  echo ""
fi

exit 0
