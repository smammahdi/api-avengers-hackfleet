#!/bin/bash

# Build All Microservices Script - Docker Version
# This script builds all services using Docker Compose

set -e

# Navigate to project root
cd "$(dirname "$0")/../.."

echo "========================================="
echo "Building All Microservices with Docker"
echo "========================================="
echo ""

# Color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to kill process on port
kill_port() {
    local port=$1
    echo -e "${YELLOW}Checking port $port...${NC}"

    # Find and kill process on the port (macOS/Linux compatible)
    if command -v lsof &> /dev/null; then
        pid=$(lsof -ti:$port)
        if [ ! -z "$pid" ]; then
            echo -e "${YELLOW}Killing process on port $port (PID: $pid)${NC}"
            kill -9 $pid 2>/dev/null || true
            sleep 1
        fi
    fi
}

# Stop and remove existing containers
echo -e "${BLUE}Stopping existing containers...${NC}"
docker-compose --profile full down 2>/dev/null || true
echo ""

# Clean up ports used by services
echo -e "${BLUE}Cleaning up ports...${NC}"
ports=(3001 8080 8081 8082 8083 8084 8085 8086 8087 8761 8888 5432 5433 5434 5435 6379 5672 15672 9411 9090 3000 3100)

for port in "${ports[@]}"; do
    kill_port $port &
done
wait
echo -e "${GREEN}Ports cleaned${NC}"
echo ""

# Build all services with Docker
echo -e "${BLUE}Building all services with Docker Compose...${NC}"
docker-compose --profile full build

echo ""
echo "========================================="
echo -e "${GREEN}All services built successfully!${NC}"
echo "========================================="
echo ""
echo "Next steps:"
echo "1. Run: ./scripts/build/quick-start.sh"
echo "   OR"
echo "   Run: docker-compose --profile full up -d"
echo ""
echo "2. Wait 2-3 minutes for services to start"
echo ""
echo "3. Access the application:"
echo "   - API Gateway: http://localhost:8080"
echo "   - Eureka: http://localhost:8761"
echo "   - Grafana: http://localhost:3000 (admin/admin)"
echo "   - Zipkin: http://localhost:9411"
echo "   - Prometheus: http://localhost:9090"
echo "   - Kibana: http://localhost:5601"
echo "   - RabbitMQ: http://localhost:15672 (guest/guest)"
echo ""
