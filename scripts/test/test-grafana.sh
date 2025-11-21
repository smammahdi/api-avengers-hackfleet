#!/bin/bash
# Test Grafana connectivity and health

echo "Testing Grafana..."
GRAFANA_URL="http://localhost:3001"

# Check if Grafana is running
if curl -s "${GRAFANA_URL}/api/health" | grep -q "ok"; then
    echo "✅ Grafana is healthy"
    exit 0
else
    echo "❌ Grafana is not responding"
    exit 1
fi
