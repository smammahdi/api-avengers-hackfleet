#!/bin/bash

###############################################################################
# CareForAll Donation Platform - Build and Test All Services
###############################################################################
# This script builds all microservices and runs unit tests
# Use this for local development and CI/CD pipelines
###############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  CareForAll Platform - Build & Test All Services${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

# List of services to build
SERVICES=(
    "eureka-server"
    "config-server"
    "api-gateway"
    "campaign-service"
    "donation-service"
    "payment-service"
    "analytics-service"
    "auth-service"
    "notification-service"
)

BUILD_SUCCESS=()
BUILD_FAILED=()
TEST_SUCCESS=()
TEST_FAILED=()

###############################################################################
# Build and test a single service
###############################################################################
build_service() {
    local service=$1
    local service_dir="$PROJECT_ROOT/services/$service"

    if [ ! -d "$service_dir" ]; then
        echo -e "  ${RED}❌ Service directory not found: $service${NC}"
        BUILD_FAILED+=("$service")
        return 1
    fi

    echo -e "${YELLOW}[BUILD] $service${NC}"
    cd "$service_dir"

    # Clean and compile
    if mvn clean compile -q 2>&1 | grep -q "BUILD SUCCESS"; then
        echo -e "  ${GREEN}✅ Build SUCCESS${NC}"
        BUILD_SUCCESS+=("$service")

        # Run tests
        echo -e "  ${BLUE}[TEST] Running unit tests...${NC}"
        if mvn test -q 2>&1 | grep -q "BUILD SUCCESS"; then
            test_count=$(grep -r "@Test" src/test/java 2>/dev/null | wc -l || echo "0")
            echo -e "  ${GREEN}✅ Tests PASSED ($test_count tests)${NC}"
            TEST_SUCCESS+=("$service")
        else
            echo -e "  ${YELLOW}⚠ Some tests may have failed${NC}"
            TEST_FAILED+=("$service")
        fi
    else
        echo -e "  ${RED}❌ Build FAILED${NC}"
        BUILD_FAILED+=("$service")
        return 1
    fi

    echo ""
    cd "$PROJECT_ROOT"
}

###############################################################################
# Main build loop
###############################################################################
main() {
    echo "Building all microservices..."
    echo ""

    for service in "${SERVICES[@]}"; do
        build_service "$service" || true
    done

    # Print summary
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  Build & Test Summary${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo ""

    echo -e "${GREEN}✅ Builds Successful (${#BUILD_SUCCESS[@]}/${#SERVICES[@]}):${NC}"
    for service in "${BUILD_SUCCESS[@]}"; do
        echo "     - $service"
    done
    echo ""

    if [ ${#BUILD_FAILED[@]} -gt 0 ]; then
        echo -e "${RED}❌ Builds Failed (${#BUILD_FAILED[@]}):${NC}"
        for service in "${BUILD_FAILED[@]}"; do
            echo "     - $service"
        done
        echo ""
    fi

    echo -e "${GREEN}✅ Tests Passed (${#TEST_SUCCESS[@]}):${NC}"
    for service in "${TEST_SUCCESS[@]}"; do
        echo "     - $service"
    done
    echo ""

    if [ ${#TEST_FAILED[@]} -gt 0 ]; then
        echo -e "${YELLOW}⚠ Tests with Issues (${#TEST_FAILED[@]}):${NC}"
        for service in "${TEST_FAILED[@]}"; do
            echo "     - $service"
        done
        echo ""
    fi

    # Overall status
    if [ ${#BUILD_FAILED[@]} -eq 0 ]; then
        echo -e "${GREEN}✅ ALL SERVICES BUILT SUCCESSFULLY!${NC}"
        echo ""
        echo "Next steps:"
        echo "  1. Start services: docker-compose up -d"
        echo "  2. Run integration tests: ./scripts/test/test-key-scenarios.sh"
        echo "  3. View traces: http://localhost:9411 (Zipkin)"
        echo "  4. View metrics: http://localhost:3000 (Grafana, admin/admin)"
        return 0
    else
        echo -e "${RED}❌ SOME SERVICES FAILED TO BUILD${NC}"
        echo "Please check the build logs above and fix the errors."
        return 1
    fi
}

# Run main
main
