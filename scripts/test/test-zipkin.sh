#!/bin/bash
# Test Zipkin connectivity and health

echo "Testing Zipkin..."
ZIPKIN_URL="http://localhost:9411"

# Check if Zipkin is running
if curl -s "${ZIPKIN_URL}/health" | grep -q "UP"; then
    echo "✅ Zipkin is healthy"
    exit 0
else
    echo "❌ Zipkin is not responding"
    exit 1
fi
