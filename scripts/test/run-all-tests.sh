#!/bin/bash

###############################################################################
# Master Test Runner - Runs All Test Scenarios
###############################################################################
# This script runs all test scenarios in sequence and provides a summary
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTS_DIR="$SCRIPT_DIR/test-results"
mkdir -p "$RESULTS_DIR"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                                                           ║${NC}"
echo -e "${CYAN}║        CareForAll Platform - Master Test Suite           ║${NC}"
echo -e "${CYAN}║                                                           ║${NC}"
echo -e "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
echo ""

# Test results tracking
declare -A test_results
declare -A test_times

###############################################################################
# Run a test suite
###############################################################################
run_test_suite() {
    local test_name=$1
    local test_script=$2

    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  Running: $test_name${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo ""

    local start_time=$(date +%s)

    if [ ! -f "$test_script" ]; then
        echo -e "${YELLOW}⚠ Test script not found: $test_script${NC}"
        test_results[$test_name]="SKIP"
        return 0
    fi

    if bash "$test_script" > "$RESULTS_DIR/${test_name// /-}.log" 2>&1; then
        test_results[$test_name]="PASS"
        echo -e "${GREEN}✅ $test_name PASSED${NC}"
    else
        test_results[$test_name]="FAIL"
        echo -e "${RED}❌ $test_name FAILED${NC}"
        echo "  Check log: $RESULTS_DIR/${test_name// /-}.log"
    fi

    local end_time=$(date +%s)
    test_times[$test_name]=$((end_time - start_time))

    echo ""
}

###############################################################################
# Check if services are running
###############################################################################
check_services() {
    echo -e "${YELLOW}Checking service availability...${NC}"
    echo ""

    local services_running=0
    local services_total=0

    services=(
        "8080:API Gateway"
        "8082:Campaign Service"
        "8085:Donation Service"
        "8086:Payment Service"
        "8087:Analytics Service"
        "8089:Auth Service"
        "8761:Eureka Server"
        "5672:RabbitMQ"
        "9411:Zipkin"
    )

    for service in "${services[@]}"; do
        port="${service%%:*}"
        name="${service#*:}"
        ((services_total++))

        if nc -z localhost "$port" 2>/dev/null; then
            echo -e "  ${GREEN}✅ $name (port $port)${NC}"
            ((services_running++))
        else
            echo -e "  ${RED}❌ $name (port $port)${NC}"
        fi
    done

    echo ""
    echo "Services running: $services_running/$services_total"

    if [ $services_running -lt 5 ]; then
        echo -e "${YELLOW}⚠ WARNING: Not all services are running${NC}"
        echo "  To start services: docker-compose up -d"
        echo "  Some tests may be skipped"
        echo ""
    fi
}

###############################################################################
# Run all test suites
###############################################################################
run_all_tests() {
    # Check services first
    check_services

    echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  Starting Test Execution${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
    echo ""

    # Run test suites
    run_test_suite "Component Tests" "$SCRIPT_DIR/component-tests.sh"
    run_test_suite "Problem 1: Idempotency" "$SCRIPT_DIR/problem-1-idempotency-tests.sh"
    run_test_suite "Problem 2: Outbox Pattern" "$SCRIPT_DIR/problem-2-outbox-tests.sh"
    run_test_suite "End-to-End Flow" "$SCRIPT_DIR/end-to-end-donation-flow.sh"
    run_test_suite "Key Scenarios" "$SCRIPT_DIR/test-key-scenarios.sh"
}

###############################################################################
# Print summary
###############################################################################
print_summary() {
    echo -e "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                                                           ║${NC}"
    echo -e "${CYAN}║                     Test Summary                          ║${NC}"
    echo -e "${CYAN}║                                                           ║${NC}"
    echo -e "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""

    local total=0
    local passed=0
    local failed=0
    local skipped=0

    echo "Test Results:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    for test_name in "${!test_results[@]}"; do
        result="${test_results[$test_name]}"
        time="${test_times[$test_name]}"

        ((total++))

        case $result in
            PASS)
                echo -e "${GREEN}✅ PASS${NC}  $test_name (${time}s)"
                ((passed++))
                ;;
            FAIL)
                echo -e "${RED}❌ FAIL${NC}  $test_name (${time}s)"
                ((failed++))
                ;;
            SKIP)
                echo -e "${YELLOW}⏭ SKIP${NC}  $test_name"
                ((skipped++))
                ;;
        esac
    done

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""

    echo "Statistics:"
    echo "  Total Tests:    $total"
    echo -e "  ${GREEN}Passed:         $passed${NC}"
    echo -e "  ${RED}Failed:         $failed${NC}"
    echo -e "  ${YELLOW}Skipped:        $skipped${NC}"
    echo ""

    if [ $failed -eq 0 ] && [ $passed -gt 0 ]; then
        echo -e "${GREEN}╔═══════════════════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║                                                           ║${NC}"
        echo -e "${GREEN}║              ✅ ALL TESTS PASSED! ✅                       ║${NC}"
        echo -e "${GREEN}║                                                           ║${NC}"
        echo -e "${GREEN}║     CareForAll Platform is Production Ready! 🎉          ║${NC}"
        echo -e "${GREEN}║                                                           ║${NC}"
        echo -e "${GREEN}╚═══════════════════════════════════════════════════════════╝${NC}"
        return 0
    elif [ $failed -gt 0 ]; then
        echo -e "${RED}╔═══════════════════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║                                                           ║${NC}"
        echo -e "${RED}║                ❌ SOME TESTS FAILED ❌                     ║${NC}"
        echo -e "${RED}║                                                           ║${NC}"
        echo -e "${RED}╚═══════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo "Check logs in: $RESULTS_DIR"
        return 1
    else
        echo -e "${YELLOW}╔═══════════════════════════════════════════════════════════╗${NC}"
        echo -e "${YELLOW}║                                                           ║${NC}"
        echo -e "${YELLOW}║         ⚠ NO TESTS RUN (Services Not Running)            ║${NC}"
        echo -e "${YELLOW}║                                                           ║${NC}"
        echo -e "${YELLOW}╚═══════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo "To run tests:"
        echo "  1. Start services: docker-compose up -d"
        echo "  2. Wait for startup: sleep 180"
        echo "  3. Run tests again: ./scripts/test/run-all-tests.sh"
        return 0
    fi
}

###############################################################################
# Main
###############################################################################
main() {
    local start_time=$(date +%s)

    run_all_tests
    print_summary

    local end_time=$(date +%s)
    local total_time=$((end_time - start_time))

    echo ""
    echo "Total execution time: ${total_time}s"
    echo "Results saved to: $RESULTS_DIR"
    echo ""
}

# Run main
main
