#!/bin/bash

# check-ports.sh - Check running containers and suggest available port offsets
# This script helps identify port conflicts and suggests available PORT_OFFSET values.

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Sandbox Docker Environment - Port Usage Check ===${NC}\n"

# Check for running containers with sandbox prefix
echo -e "${YELLOW}Running Containers:${NC}"
CONTAINERS=$(docker ps --format "table {{.Names}}\t{{.Ports}}" | grep -E "(sandbox|NAMES)" || echo "No sandbox containers running")
echo "$CONTAINERS"
echo ""

# Base ports
FRONTEND_BASE=3000
BACKEND_BASE=8080
POSTGRES_BASE=5432
DEBUG_BASE=5005
NGINX_BASE=80

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -iTCP:$port -sTCP:LISTEN >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is available
    fi
}

# Function to get process name using a port
get_port_process() {
    local port=$1
    lsof -iTCP:$port -sTCP:LISTEN 2>/dev/null | awk 'NR==2 {print $1}' || echo "unknown"
}

# Check common offset values
echo -e "${YELLOW}Port Availability Check:${NC}"
printf "%-10s %-10s %-10s %-10s %-10s %-10s %-15s\n" "Offset" "Frontend" "Backend" "PostgreSQL" "Debug" "Nginx" "Status"
echo "--------------------------------------------------------------------------------"

OFFSETS=(0 1 100 200 300 400 500)
AVAILABLE_OFFSETS=()

for OFFSET in "${OFFSETS[@]}"; do
    FRONTEND_PORT=$((FRONTEND_BASE + OFFSET))
    BACKEND_PORT=$((BACKEND_BASE + OFFSET))
    POSTGRES_PORT=$((POSTGRES_BASE + OFFSET))
    DEBUG_PORT=$((DEBUG_BASE + OFFSET))
    NGINX_PORT=$((NGINX_BASE + OFFSET))

    ALL_AVAILABLE=true
    CONFLICTS=""

    # Check each port
    for PORT in $FRONTEND_PORT $BACKEND_PORT $POSTGRES_PORT $DEBUG_PORT $NGINX_PORT; do
        if check_port $PORT; then
            ALL_AVAILABLE=false
            PROCESS=$(get_port_process $PORT)
            if [ -z "$CONFLICTS" ]; then
                CONFLICTS="$PORT($PROCESS)"
            else
                CONFLICTS="$CONFLICTS, $PORT($PROCESS)"
            fi
        fi
    done

    if $ALL_AVAILABLE; then
        printf "%-10s %-10s %-10s %-10s %-10s %-10s ${GREEN}%-15s${NC}\n" \
            "$OFFSET" "$FRONTEND_PORT" "$BACKEND_PORT" "$POSTGRES_PORT" "$DEBUG_PORT" "$NGINX_PORT" "✓ Available"
        AVAILABLE_OFFSETS+=($OFFSET)
    else
        printf "%-10s %-10s %-10s %-10s %-10s %-10s ${RED}%-15s${NC}\n" \
            "$OFFSET" "$FRONTEND_PORT" "$BACKEND_PORT" "$POSTGRES_PORT" "$DEBUG_PORT" "$NGINX_PORT" "✗ In use"
        echo "           Conflicts: $CONFLICTS"
    fi
done

echo ""

# Suggest next available offset
if [ ${#AVAILABLE_OFFSETS[@]} -eq 0 ]; then
    echo -e "${RED}No standard offsets are available. Try a custom offset (e.g., 600, 700).${NC}"
else
    echo -e "${GREEN}Recommended available offsets:${NC}"
    for OFFSET in "${AVAILABLE_OFFSETS[@]}"; do
        case $OFFSET in
            0)
                echo "  - $OFFSET (Main development environment)"
                ;;
            1)
                echo "  - $OFFSET (E2E testing)"
                ;;
            *)
                echo "  - $OFFSET (Worktree environment)"
                ;;
        esac
    done
fi

echo ""
echo -e "${YELLOW}Docker Volumes:${NC}"
docker volume ls | grep -E "(sandbox|VOLUME)" || echo "No sandbox volumes found"

echo ""
echo -e "${BLUE}Tip: Use './scripts/setup-worktree-env.sh' to automatically configure a new worktree environment.${NC}"
