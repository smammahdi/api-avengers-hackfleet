#!/bin/bash

# Stop All Services Script

# Navigate to project root
cd "$(dirname "$0")/../.."

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  Stopping All Services${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Stop Docker Compose
echo -e "${YELLOW}Stopping Docker containers...${NC}"
docker-compose down

echo ""
echo -e "${GREEN}✓ All services stopped${NC}"
echo ""
echo -e "${BLUE}To also remove volumes (databases), run:${NC}"
echo "  docker-compose down -v"
echo ""
echo -e "${BLUE}To remove all images and start fresh, run:${NC}"
echo "  docker-compose down -v"
echo "  docker system prune -a"
