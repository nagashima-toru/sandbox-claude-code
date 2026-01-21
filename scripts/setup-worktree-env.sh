#!/bin/bash

# setup-worktree-env.sh - Automatically configure environment for a git worktree
# This script creates .env and frontend/.env.local files with unique settings
# to avoid conflicts when running multiple environments simultaneously.

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo -e "${BLUE}=== Sandbox Worktree Environment Setup ===${NC}\n"

# Check if we're in the project root
if [ ! -f "$PROJECT_ROOT/docker-compose.yml" ]; then
    echo -e "${RED}Error: docker-compose.yml not found. Are you in the project root?${NC}"
    exit 1
fi

cd "$PROJECT_ROOT"

# Parse command line arguments
REGENERATE=false
if [ "$1" = "--regenerate" ]; then
    REGENERATE=true
fi

# Function to check if a port is in use
check_port() {
    local port=$1
    lsof -iTCP:$port -sTCP:LISTEN >/dev/null 2>&1
}

# Function to find available port offset
find_available_offset() {
    local offsets=(0 1 100 200 300 400 500 600 700 800 900)
    local base_ports=(3000 8080 5432 5005 80)

    for offset in "${offsets[@]}"; do
        local all_available=true
        for base in "${base_ports[@]}"; do
            local port=$((base + offset))
            if check_port $port; then
                all_available=false
                break
            fi
        done

        if $all_available; then
            echo $offset
            return 0
        fi
    done

    # If no standard offset is available, suggest a high value
    echo 1000
    return 1
}

# Get current branch name
BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")

# Generate default project name from branch
# Replace slashes and special chars with hyphens
DEFAULT_PROJECT_NAME=$(echo "$BRANCH_NAME" | sed 's/[^a-zA-Z0-9-]/-/g' | sed 's/^-//;s/-$//')

# Limit length to 30 characters
DEFAULT_PROJECT_NAME=$(echo "$DEFAULT_PROJECT_NAME" | cut -c1-30)

# Add prefix if not already present
if [[ ! "$DEFAULT_PROJECT_NAME" =~ ^sandbox ]]; then
    DEFAULT_PROJECT_NAME="sandbox-$DEFAULT_PROJECT_NAME"
fi

# Find available port offset
SUGGESTED_OFFSET=$(find_available_offset)

# Interactive configuration (unless regenerating)
if [ "$REGENERATE" = false ]; then
    echo -e "${YELLOW}Current branch:${NC} $BRANCH_NAME"
    echo ""

    # Project name
    echo -e "${YELLOW}Enter project name (used for container/volume naming):${NC}"
    echo -e "  Default: ${GREEN}$DEFAULT_PROJECT_NAME${NC}"
    read -p "  Project name: " PROJECT_NAME
    PROJECT_NAME=${PROJECT_NAME:-$DEFAULT_PROJECT_NAME}

    # Port offset
    echo ""
    echo -e "${YELLOW}Enter port offset to avoid conflicts:${NC}"
    echo -e "  Suggested: ${GREEN}$SUGGESTED_OFFSET${NC} (next available)"
    echo -e "  Common values: 0 (main), 1 (e2e), 100, 200, 300, ..."
    echo ""
    echo "  Resulting ports will be:"
    echo "    Frontend:   $((3000 + SUGGESTED_OFFSET))"
    echo "    Backend:    $((8080 + SUGGESTED_OFFSET))"
    echo "    PostgreSQL: $((5432 + SUGGESTED_OFFSET))"
    echo "    Debug:      $((5005 + SUGGESTED_OFFSET))"
    echo "    Nginx:      $((80 + SUGGESTED_OFFSET))"
    echo ""
    read -p "  Port offset: " PORT_OFFSET
    PORT_OFFSET=${PORT_OFFSET:-$SUGGESTED_OFFSET}

    # Database persistence mode
    echo ""
    echo -e "${YELLOW}Database persistence mode:${NC}"
    echo "  1) volume - Persistent storage (default, data survives restart)"
    echo "  2) tmpfs  - Temporary memory storage (faster, data lost on restart)"
    read -p "  Choice [1]: " DB_MODE_CHOICE
    DB_MODE_CHOICE=${DB_MODE_CHOICE:-1}

    if [ "$DB_MODE_CHOICE" = "2" ]; then
        DB_PERSISTENCE_MODE="tmpfs"
    else
        DB_PERSISTENCE_MODE="volume"
    fi
else
    # Regenerate mode: read from existing .env
    if [ -f "$PROJECT_ROOT/.env" ]; then
        source "$PROJECT_ROOT/.env"
        echo -e "${YELLOW}Regenerating configuration from existing .env${NC}"
        echo -e "  Project name: ${GREEN}$COMPOSE_PROJECT_NAME${NC}"
        echo -e "  Port offset: ${GREEN}$PORT_OFFSET${NC}"
        PROJECT_NAME=$COMPOSE_PROJECT_NAME
    else
        echo -e "${RED}Error: .env file not found. Run without --regenerate first.${NC}"
        exit 1
    fi
fi

# Check for port conflicts
echo ""
echo -e "${YELLOW}Checking for port conflicts...${NC}"
CONFLICTS=()
for BASE_PORT in 3000 8080 5432 5005 80; do
    PORT=$((BASE_PORT + PORT_OFFSET))
    if check_port $PORT; then
        PROCESS=$(lsof -iTCP:$PORT -sTCP:LISTEN 2>/dev/null | awk 'NR==2 {print $1}' || echo "unknown")
        CONFLICTS+=("$PORT ($PROCESS)")
    fi
done

if [ ${#CONFLICTS[@]} -gt 0 ]; then
    echo -e "${RED}Warning: The following ports are already in use:${NC}"
    for CONFLICT in "${CONFLICTS[@]}"; do
        echo "  - $CONFLICT"
    done
    echo ""
    read -p "Continue anyway? [y/N]: " CONTINUE
    if [[ ! "$CONTINUE" =~ ^[Yy]$ ]]; then
        echo "Setup cancelled."
        exit 1
    fi
fi

# Calculate port numbers
FRONTEND_PORT=$((3000 + PORT_OFFSET))
BACKEND_PORT=$((8080 + PORT_OFFSET))
POSTGRES_PORT=$((5432 + PORT_OFFSET))
DEBUG_PORT=$((5005 + PORT_OFFSET))
NGINX_PORT=$((80 + PORT_OFFSET))

# Create .env file
echo ""
echo -e "${GREEN}Creating .env file...${NC}"
cat > "$PROJECT_ROOT/.env" <<EOF
# Docker Compose Project Configuration
# Auto-generated by setup-worktree-env.sh on $(date)
# Branch: $BRANCH_NAME

COMPOSE_PROJECT_NAME=$PROJECT_NAME
PORT_OFFSET=$PORT_OFFSET

# Calculated Port Numbers
# These are automatically calculated from PORT_OFFSET
FRONTEND_PORT=$FRONTEND_PORT
BACKEND_PORT=$BACKEND_PORT
POSTGRES_PORT=$POSTGRES_PORT
DEBUG_PORT=$DEBUG_PORT
NGINX_PORT=$NGINX_PORT

# Database Configuration
POSTGRES_DB=sandbox
POSTGRES_USER=sandbox
POSTGRES_PASSWORD=sandbox
DB_PERSISTENCE_MODE=${DB_PERSISTENCE_MODE:-volume}
EOF

echo -e "  ✓ Created: ${GREEN}.env${NC}"

# Create frontend/.env.local
FRONTEND_DIR="$PROJECT_ROOT/frontend"
if [ -d "$FRONTEND_DIR" ]; then
    echo -e "${GREEN}Creating frontend/.env.local...${NC}"

    cat > "$FRONTEND_DIR/.env.local" <<EOF
# Frontend Environment Configuration
# Auto-generated by setup-worktree-env.sh on $(date)
# This file is gitignored and safe for local customization.

# API URL for browser requests
# Note: NEXT_PUBLIC_* variables are embedded at build time
NEXT_PUBLIC_API_URL=http://localhost:$BACKEND_PORT
EOF

    echo -e "  ✓ Created: ${GREEN}frontend/.env.local${NC}"
else
    echo -e "${YELLOW}  ⚠ frontend/ directory not found, skipping .env.local${NC}"
fi

# Display summary
echo ""
echo -e "${BLUE}=== Setup Complete ===${NC}"
echo ""
echo -e "${YELLOW}Configuration Summary:${NC}"
echo "  Project Name:    $PROJECT_NAME"
echo "  Port Offset:     $PORT_OFFSET"
echo "  DB Mode:         ${DB_PERSISTENCE_MODE:-volume}"
echo ""
echo -e "${YELLOW}Access URLs:${NC}"
echo "  Frontend:        http://localhost:$FRONTEND_PORT"
echo "  Backend API:     http://localhost:$BACKEND_PORT/api/messages"
echo "  Nginx:           http://localhost:$NGINX_PORT"
echo "  Backend Debug:   localhost:$DEBUG_PORT"
echo "  PostgreSQL:      localhost:$POSTGRES_PORT"
echo ""
echo -e "${YELLOW}Docker Resources:${NC}"
echo "  Containers:      ${PROJECT_NAME}-*"
echo "  Volume:          ${PROJECT_NAME}_postgres-data"
echo "  Network:         ${PROJECT_NAME}_default"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "  1. Start environment:     ${GREEN}docker-compose up${NC}"
echo "  2. View logs:             ${GREEN}docker-compose logs -f${NC}"
echo "  3. Stop environment:      ${GREEN}docker-compose down${NC}"
echo "  4. Check port usage:      ${GREEN}./scripts/check-ports.sh${NC}"
echo ""

if [ "$DB_PERSISTENCE_MODE" = "tmpfs" ]; then
    echo -e "${RED}Note: Using tmpfs mode - all database data will be lost on container stop!${NC}"
    echo ""
fi

# Warn about NEXT_PUBLIC_* rebuild requirement
echo -e "${YELLOW}Important:${NC}"
echo "  - If you change PORT_OFFSET later, re-run: ${GREEN}./scripts/setup-worktree-env.sh --regenerate${NC}"
echo "  - Then rebuild frontend: ${GREEN}docker-compose build frontend${NC}"
echo "  - NEXT_PUBLIC_* variables are embedded at build time"
echo ""
