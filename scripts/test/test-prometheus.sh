#!/bin/bash
# Test Prometheus connectivity and health

echo "Testing Prometheus..."
PROMETHEUS_URL="http://localhost:9090"

# Check if Prometheus is running
if curl -s "${PROMETHEUS_URL}/-/healthy" | grep -q "Prometheus"; then
    echo "✅ Prometheus is healthy"
    exit 0
else
    echo "❌ Prometheus is not responding"
    exit 1
fi
