#!/bin/bash
# Test RabbitMQ connectivity and health

echo "Testing RabbitMQ..."
RABBITMQ_URL="http://localhost:15672"

# Check if RabbitMQ management interface is running
if curl -s -u guest:guest "${RABBITMQ_URL}/api/health/checks/alarms" | grep -q "ok"; then
    echo "✅ RabbitMQ is healthy"
    exit 0
else
    echo "❌ RabbitMQ is not responding"
    exit 1
fi
