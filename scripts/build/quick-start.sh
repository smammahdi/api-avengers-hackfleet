#!/bin/bash

# Quick Start Script - Docker Version
# Builds and starts all services using Docker Compose

set -e

# Navigate to project root
cd "$(dirname "$0")/../.."

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  HackFleet Donation Platform${NC}"
echo -e "${BLUE}  Quick Start (Docker - Local Build)${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
  echo -e "${YELLOW}⚠ Docker is not running. Please start Docker Desktop.${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Docker is running${NC}"
echo ""

# Function to kill process on port
kill_port() {
    local port=$1
    if command -v lsof &> /dev/null; then
        pid=$(lsof -ti:$port 2>/dev/null)
        if [ ! -z "$pid" ]; then
            echo -e "${YELLOW}  Killing process on port $port${NC}"
            kill -9 $pid 2>/dev/null || true
            sleep 1
        fi
    fi
}

# Stop and clean up existing containers
echo -e "${BLUE}Step 1/4: Stopping existing containers...${NC}"
docker-compose down 2>/dev/null || true
echo ""

# Clean up ports
echo -e "${BLUE}Step 2/4: Cleaning up ports...${NC}"
ports=(3001 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8091 8761 8888 5432 5433 5434 5435 5436 27017 6379 5672 15672 9411 9090 3000 9200 9300 5601 9600)
for port in "${ports[@]}"; do
    kill_port $port &
done
wait
echo -e "${GREEN}  Ports cleaned${NC}"
echo ""

# Build all services locally
echo -e "${BLUE}Step 3/4: Building all services locally with Docker...${NC}"
echo -e "${YELLOW}  This will build images from local source code...${NC}"
docker-compose build
echo ""

# Start all services
echo -e "${BLUE}Step 4/4: Starting Docker containers...${NC}"
docker-compose up -d

echo ""
echo -e "${BLUE}Waiting for services to be healthy...${NC}"
echo -e "${YELLOW}This may take 2-3 minutes...${NC}"
sleep 30

# Check service health
echo ""
echo -e "${GREEN}Checking service health:${NC}"

services=(
  "hackfleet-eureka-server:8761"
  "hackfleet-config-server:8888"
  "hackfleet-api-gateway:8080"
  "hackfleet-campaign-service:8082"
  "hackfleet-donation-service:8085"
  "hackfleet-payment-service:8086"
  "hackfleet-banking-service:8091"
  "hackfleet-analytics-service:8087"
  "hackfleet-auth-service:8089"
  "hackfleet-notification-service:8088"
  "hackfleet-frontend:3001"
)

for service in "${services[@]}"
do
  name="${service%%:*}"
  port="${service##*:}"

  if [ "$name" = "hackfleet-frontend" ]; then
    # Check frontend with simple HTTP request
    if curl -s "http://localhost:$port" > /dev/null 2>&1; then
      echo -e "  ${GREEN}✓${NC} $name (port $port)"
    else
      echo -e "  ${YELLOW}⏳${NC} $name (port $port) - still starting..."
    fi
  else
    # Check Spring Boot services with actuator
    if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
      echo -e "  ${GREEN}✓${NC} $name (port $port)"
    else
      echo -e "  ${YELLOW}⏳${NC} $name (port $port) - still starting..."
    fi
  fi
done

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  All services started!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "${BLUE}Access points:${NC}"
echo "  • Frontend UI:       http://localhost:3001"
echo "  • API Gateway:       http://localhost:8080"
echo "  • Eureka Dashboard:  http://localhost:8761"
echo "  • Zipkin Tracing:    http://localhost:9411"
echo "  • Prometheus:        http://localhost:9090"
echo "  • Grafana:           http://localhost:3000 (admin/admin)"
echo "  • RabbitMQ:          http://localhost:15672 (admin/admin)"
echo ""
echo -e "${BLUE}Quick test:${NC}"
echo "  1. Open http://localhost:3001 in your browser"
echo ""
echo "  OR via API:"
echo ""
echo "  1. Register: curl -X POST http://localhost:8080/api/users/register \\"
echo "       -H 'Content-Type: application/json' \\"
echo "       -d '{\"email\":\"test@example.com\",\"password\":\"password123\",\"firstName\":\"John\",\"lastName\":\"Doe\"}'"
echo ""
echo "  2. Login:    curl -X POST http://localhost:8080/api/users/login \\"
echo "       -H 'Content-Type: application/json' \\"
echo "       -d '{\"email\":\"test@example.com\",\"password\":\"password123\"}'"
echo ""
echo "  3. See test-api.sh for more examples"
echo ""
echo -e "${GREEN}Happy microservicing! 🚀${NC}"
