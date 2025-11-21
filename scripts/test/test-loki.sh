#!/bin/bash
# Test Loki connectivity and health

echo "Testing Loki..."
LOKI_URL="http://localhost:3100"

# Check if Loki is running
if curl -s "${LOKI_URL}/ready" | grep -q "ready"; then
    echo "✅ Loki is healthy"
    exit 0
else
    echo "❌ Loki is not responding"
    exit 1
fi
