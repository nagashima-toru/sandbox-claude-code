#!/bin/bash

################################################################################
# Docker Development Environment Launcher
#
# Start the Docker development environment with hot reload enabled.
# This script launches docker compose in development mode using docker-compose.yml
# and docker-compose.override.yml (auto-loaded).
#
# Usage:
#   ./scripts/docker-dev.sh [OPTIONS]
#
# Options:
#   -d, --detach         Run containers in background (detached mode)
#   --build              Force rebuild of images before starting
#   --help               Show this help message
#
# Examples:
#   # Start in foreground (logs visible)
#   ./scripts/docker-dev.sh
#
#   # Start in background
#   ./scripts/docker-dev.sh -d
#
#   # Rebuild and start
#   ./scripts/docker-dev.sh --build
#
# Features:
#   - Hot reload for frontend and backend
#   - Source code mounted as volumes
#   - Debug port exposed (5005)
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
      head -n 40 "$0" | grep "^#" | sed 's/^# //; s/^#//'
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

# Check if .env file exists (optional, but warn if missing)
if [ ! -f ".env" ]; then
  print_warning ".env file not found - using default values"
fi

print_success "Environment validation passed"

# Build Docker command
DOCKER_CMD="docker compose up"

if [ "$BUILD_MODE" = true ]; then
  DOCKER_CMD="$DOCKER_CMD --build"
fi

if [ "$DETACH_MODE" = true ]; then
  DOCKER_CMD="$DOCKER_CMD -d"
fi

# Show what will be executed
echo ""
print_info "Starting Docker development environment..."
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
  echo "  Frontend: http://localhost:3000"
  echo "  Backend:  http://localhost:8080"
  echo "  Debug:    localhost:5005 (Java Debug Wire Protocol)"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""
  print_info "View logs: docker compose logs -f"
  print_info "Stop:      docker compose down"
  echo ""
fi

exit 0
