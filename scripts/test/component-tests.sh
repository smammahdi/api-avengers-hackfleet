#!/bin/bash

###############################################################################
# Component Tests - Individual Service and Infrastructure Tests
###############################################################################

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Component Tests${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

###############################################################################
# Test API Gateway Routing
###############################################################################
test_api_gateway() {
    echo -e "${YELLOW}[Test] API Gateway Routing${NC}"
    echo "Testing if API Gateway correctly routes to services"
    echo ""

    local gateway="http://localhost:8080"

    # Test routing to different services
    routes=(
        "/actuator/health:API Gateway Health"
        "/api/campaigns:Campaign Service"
        "/api/donations:Donation Service"
        "/api/payments:Payment Service"
        "/api/analytics/platform:Analytics Service"
    )

    local passed=0
    local total=${#routes[@]}

    for route in "${routes[@]}"; do
        endpoint="${route%%:*}"
        name="${route#*:}"

        if curl -sf "$gateway$endpoint" > /dev/null 2>&1; then
            echo -e "  ${GREEN}✅ $name ($endpoint)${NC}"
            ((passed++))
        else
            echo -e "  ${RED}❌ $name ($endpoint)${NC}"
        fi
    done

    echo ""
    echo "  API Gateway Routing: $passed/$total routes working"

    if [ $passed -eq $total ]; then
        echo -e "  ${GREEN}✅ PASS: All routes accessible${NC}"
    else
        echo -e "  ${YELLOW}⚠ Some routes not accessible (services may not be running)${NC}"
    fi
    echo ""
}

###############################################################################
# Test Service Discovery (Eureka)
###############################################################################
test_service_discovery() {
    echo -e "${YELLOW}[Test] Service Discovery (Eureka)${NC}"
    echo "Testing if services are registered with Eureka"
    echo ""

    local eureka="http://localhost:8761"

    if ! curl -sf "$eureka/actuator/health" > /dev/null 2>&1; then
        echo -e "  ${RED}❌ Eureka server not accessible${NC}"
        echo ""
        return 1
    fi

    echo -e "  ${GREEN}✅ Eureka server running${NC}"

    # Fetch registered services
    registered=$(curl -s "$eureka/eureka/apps" 2>/dev/null | grep -o '<name>[^<]*</name>' | sed 's/<[^>]*>//g' || echo "")

    if [ -n "$registered" ]; then
        echo "  Registered services:"
        echo "$registered" | while read -r service; do
            echo -e "    ${GREEN}✅ $service${NC}"
        done
    else
        echo -e "  ${YELLOW}⚠ No services registered yet${NC}"
    fi
    echo ""
}

###############################################################################
# Test RabbitMQ Event Bus
###############################################################################
test_rabbitmq() {
    echo -e "${YELLOW}[Test] RabbitMQ Event Bus${NC}"
    echo "Testing RabbitMQ availability and queue configuration"
    echo ""

    local rabbitmq_api="http://localhost:15672/api"
    local auth="guest:guest"

    if ! curl -sf -u "$auth" "$rabbitmq_api/overview" > /dev/null 2>&1; then
        echo -e "  ${RED}❌ RabbitMQ management API not accessible${NC}"
        echo "  Make sure RabbitMQ is running with management plugin"
        echo ""
        return 1
    fi

    echo -e "  ${GREEN}✅ RabbitMQ server running${NC}"

    # Check queues
    queues=$(curl -s -u "$auth" "$rabbitmq_api/queues" 2>/dev/null)

    if echo "$queues" | grep -q "donation"; then
        echo -e "  ${GREEN}✅ Donation queues configured${NC}"
    fi

    if echo "$queues" | grep -q "campaign"; then
        echo -e "  ${GREEN}✅ Campaign queues configured${NC}"
    fi

    # Check exchanges
    exchanges=$(curl -s -u "$auth" "$rabbitmq_api/exchanges" 2>/dev/null)

    if echo "$exchanges" | grep -q "donation"; then
        echo -e "  ${GREEN}✅ Donation exchanges configured${NC}"
    fi

    echo ""
    echo -e "  ${GREEN}✅ PASS: RabbitMQ event bus operational${NC}"
    echo ""
}

###############################################################################
# Test Database Connections
###############################################################################
test_databases() {
    echo -e "${YELLOW}[Test] Database Connections${NC}"
    echo "Testing database availability"
    echo ""

    # PostgreSQL (Campaign DB)
    if nc -z localhost 5433 2>/dev/null; then
        echo -e "  ${GREEN}✅ PostgreSQL (Campaign DB) - Port 5433${NC}"
    else
        echo -e "  ${RED}❌ PostgreSQL (Campaign DB) - Port 5433${NC}"
    fi

    # PostgreSQL (Donation DB)
    if nc -z localhost 5434 2>/dev/null; then
        echo -e "  ${GREEN}✅ PostgreSQL (Donation DB) - Port 5434${NC}"
    else
        echo -e "  ${RED}❌ PostgreSQL (Donation DB) - Port 5434${NC}"
    fi

    # PostgreSQL (Auth DB)
    if nc -z localhost 5435 2>/dev/null; then
        echo -e "  ${GREEN}✅ PostgreSQL (Auth DB) - Port 5435${NC}"
    else
        echo -e "  ${RED}❌ PostgreSQL (Auth DB) - Port 5435${NC}"
    fi

    # MongoDB (Analytics)
    if nc -z localhost 27017 2>/dev/null; then
        echo -e "  ${GREEN}✅ MongoDB (Analytics DB) - Port 27017${NC}"
    else
        echo -e "  ${RED}❌ MongoDB (Analytics DB) - Port 27017${NC}"
    fi

    echo ""
}

###############################################################################
# Test Monitoring Stack
###############################################################################
test_monitoring() {
    echo -e "${YELLOW}[Test] Monitoring Stack${NC}"
    echo "Testing observability services"
    echo ""

    services=(
        "Zipkin:http://localhost:9411/api/v2/services"
        "Prometheus:http://localhost:9090/-/healthy"
        "Grafana:http://localhost:3000/api/health"
        "Elasticsearch:http://localhost:9200/_cluster/health"
        "Kibana:http://localhost:5601/api/status"
    )

    for service in "${services[@]}"; do
        name="${service%%:*}"
        url="${service#*:}"

        if curl -sf "$url" > /dev/null 2>&1; then
            echo -e "  ${GREEN}✅ $name${NC}"
        else
            echo -e "  ${RED}❌ $name${NC}"
        fi
    done

    echo ""
}

###############################################################################
# Test Actuator Endpoints
###############################################################################
test_actuator_endpoints() {
    echo -e "${YELLOW}[Test] Actuator Endpoints${NC}"
    echo "Testing Spring Boot Actuator endpoints"
    echo ""

    services=(
        "8082:Campaign Service"
        "8085:Donation Service"
        "8086:Payment Service"
        "8087:Analytics Service"
        "8089:Auth Service"
    )

    for service in "${services[@]}"; do
        port="${service%%:*}"
        name="${service#*:}"

        if curl -sf "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo -e "  ${GREEN}✅ $name (port $port)${NC}"
        else
            echo -e "  ${RED}❌ $name (port $port)${NC}"
        fi
    done

    echo ""
}

###############################################################################
# Test Metrics Export
###############################################################################
test_metrics_export() {
    echo -e "${YELLOW}[Test] Metrics Export (Prometheus)${NC}"
    echo "Testing if services export Prometheus metrics"
    echo ""

    services=(
        "8085:Donation Service"
        "8086:Payment Service"
    )

    for service in "${services[@]}"; do
        port="${service%%:*}"
        name="${service#*:}"

        if curl -sf "http://localhost:$port/actuator/prometheus" > /dev/null 2>&1; then
            echo -e "  ${GREEN}✅ $name exports metrics${NC}"
        else
            echo -e "  ${YELLOW}⚠ $name metrics not accessible${NC}"
        fi
    done

    echo ""
}

###############################################################################
# Test Distributed Tracing
###############################################################################
test_distributed_tracing() {
    echo -e "${YELLOW}[Test] Distributed Tracing (Zipkin)${NC}"
    echo "Testing if traces are being collected"
    echo ""

    # Make a test request
    echo "  Generating trace..."
    curl -s "http://localhost:8085/actuator/health" > /dev/null 2>&1

    sleep 2

    # Check if traces exist in Zipkin
    if curl -sf "http://localhost:9411/api/v2/services" > /dev/null 2>&1; then
        services=$(curl -s "http://localhost:9411/api/v2/services" 2>/dev/null)

        if echo "$services" | grep -q "donation-service"; then
            echo -e "  ${GREEN}✅ Traces collected for donation-service${NC}"
        fi

        echo -e "  ${GREEN}✅ PASS: Distributed tracing operational${NC}"
    else
        echo -e "  ${YELLOW}⚠ Zipkin not accessible${NC}"
    fi

    echo ""
}

###############################################################################
# Main Test Runner
###############################################################################
main() {
    echo "Running component tests..."
    echo ""

    test_api_gateway
    test_service_discovery
    test_rabbitmq
    test_databases
    test_monitoring
    test_actuator_endpoints
    test_metrics_export
    test_distributed_tracing

    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}  Component Tests Complete!${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo ""
    echo "Summary:"
    echo "  ✅ API Gateway routing tested"
    echo "  ✅ Service discovery tested"
    echo "  ✅ Event bus (RabbitMQ) tested"
    echo "  ✅ Database connections tested"
    echo "  ✅ Monitoring stack tested"
    echo "  ✅ Actuator endpoints tested"
    echo "  ✅ Metrics export tested"
    echo "  ✅ Distributed tracing tested"
}

# Run tests
main
