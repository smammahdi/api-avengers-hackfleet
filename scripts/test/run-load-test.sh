#!/bin/bash

# Run K6 Load Test Script
# This script runs the K6 load test against the e-commerce microservices

set -e

cd "$(dirname "$0")/../.."

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  E-Commerce Load Testing with K6${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Check if K6 is installed
if ! command -v k6 &> /dev/null; then
    echo -e "${RED}Error: K6 is not installed${NC}"
    echo ""
    echo "Please install K6:"
    echo "  macOS:   brew install k6"
    echo "  Linux:   sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69"
    echo "           echo 'deb https://dl.k6.io/deb stable main' | sudo tee /etc/apt/sources.list.d/k6.list"
    echo "           sudo apt-get update && sudo apt-get install k6"
    echo "  Windows: choco install k6"
    echo ""
    echo "Or download from: https://k6.io/docs/getting-started/installation/"
    exit 1
fi

echo -e "${GREEN}✓ K6 is installed${NC}"
echo ""

# Get base URL from environment or use default
BASE_URL=${BASE_URL:-http://localhost:8080}

echo -e "${BLUE}Configuration:${NC}"
echo "  Base URL: $BASE_URL"
echo ""

# Check if services are running
echo -e "${BLUE}Checking if services are running...${NC}"
if ! curl -s "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${YELLOW}Warning: Cannot reach API Gateway at ${BASE_URL}${NC}"
    echo "Please make sure services are running with: ./scripts/build/quick-start.sh"
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo -e "${GREEN}✓ API Gateway is reachable${NC}"
fi

echo ""
echo -e "${BLUE}Starting load test...${NC}"
echo -e "${YELLOW}This will take approximately 7 minutes${NC}"
echo ""

# Run the load test
k6 run \
  --out json=load-test-results.json \
  -e BASE_URL="${BASE_URL}" \
  scripts/test/load-test.js

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  Load Test Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "${BLUE}Results saved to:${NC} load-test-results.json"
echo ""
echo -e "${BLUE}To view detailed metrics:${NC}"
echo "  • Grafana:    http://localhost:3000 (admin/admin)"
echo "  • Prometheus: http://localhost:9090"
echo "  • Zipkin:     http://localhost:9411"
echo ""
echo -e "${BLUE}Recommended Grafana dashboards to check:${NC}"
echo "  1. Microservices Overview - Overall system health and performance"
echo "  2. JVM Metrics - Memory and garbage collection during load"
echo ""
