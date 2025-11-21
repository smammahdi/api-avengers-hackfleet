#!/bin/bash

# Test All Microservices Script
# Runs unit tests for all services

set -e

# Navigate to project root
cd "$(dirname "$0")/../.."

echo "========================================="
echo "Running Tests for All Microservices"
echo "========================================="
echo ""

GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

services=(
  "eureka-server"
  "config-server"
  "api-gateway"
  "user-service"
  "product-service"
  "inventory-service"
  "cart-service"
  "order-service"
  "payment-service"
  "notification-service"
)

failed_services=()

for service in "${services[@]}"
do
  echo -e "${BLUE}Testing $service...${NC}"
  cd "services/$service" || exit

  if mvn test; then
    echo -e "${GREEN}✓ $service tests passed${NC}"
  else
    echo -e "${RED}✗ $service tests failed${NC}"
    failed_services+=("$service")
  fi

  echo ""
  cd ..
done

echo "========================================="
if [ ${#failed_services[@]} -eq 0 ]; then
  echo -e "${GREEN}All tests passed!${NC}"
else
  echo -e "${RED}Failed services: ${failed_services[*]}${NC}"
  exit 1
fi
echo "========================================="
