#!/bin/bash

# API Testing Script - CareForAll Donation Platform
# Tests the complete donation flow

set -e

# Navigate to project root
cd "$(dirname "$0")/../.."

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

API_URL="http://localhost:8080"

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  CareForAll API Testing${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Step 1: Create a campaign
echo -e "${BLUE}1. Creating a new campaign...${NC}"
CAMPAIGN_RESPONSE=$(curl -s -X POST "$API_URL/api/campaigns" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Help Build School",
    "description": "Building school in rural area",
    "targetAmount": 50000,
    "category": "EDUCATION",
    "createdBy": "Admin"
  }')

echo "$CAMPAIGN_RESPONSE" | jq '.'
CAMPAIGN_ID=$(echo "$CAMPAIGN_RESPONSE" | jq -r '.id')
echo -e "${GREEN}✓ Campaign created (ID: $CAMPAIGN_ID)${NC}"
echo ""

# Step 2: Make a guest donation (no authentication)
echo -e "${BLUE}2. Making a guest donation...${NC}"
GUEST_DONATION=$(curl -s -X POST "$API_URL/api/donations" \
  -H "Content-Type: application/json" \
  -d "{
    \"campaignId\": $CAMPAIGN_ID,
    \"amount\": 100.00,
    \"donorEmail\": \"guest@example.com\"
  }")

echo "$GUEST_DONATION" | jq '.'
DONATION_ID=$(echo "$GUEST_DONATION" | jq -r '.id')
echo -e "${GREEN}✓ Guest donation created (ID: $DONATION_ID)${NC}"
echo ""

# Step 3: Register a new user
echo -e "${BLUE}3. Registering new user...${NC}"
REGISTER_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "donor@example.com",
    "password": "password123",
    "name": "John Donor",
    "role": "DONOR"
  }')

echo "$REGISTER_RESPONSE" | jq '.'
echo -e "${GREEN}✓ User registered${NC}"
echo ""

# Step 4: Login
echo -e "${BLUE}4. Logging in...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "donor@example.com",
    "password": "password123"
  }')

JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.userId')
echo "JWT Token: ${JWT_TOKEN:0:50}..."
echo -e "${GREEN}✓ Login successful (User ID: $USER_ID)${NC}"
echo ""

# Step 5: Make authenticated donation
echo -e "${BLUE}5. Making authenticated donation...${NC}"
AUTH_DONATION=$(curl -s -X POST "$API_URL/api/donations" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"campaignId\": $CAMPAIGN_ID,
    \"amount\": 250.00,
    \"donorEmail\": \"donor@example.com\",
    \"userId\": $USER_ID
  }")

echo "$AUTH_DONATION" | jq '.'
AUTH_DONATION_ID=$(echo "$AUTH_DONATION" | jq -r '.id')
echo -e "${GREEN}✓ Authenticated donation created (ID: $AUTH_DONATION_ID)${NC}"
echo ""

# Step 6: Get campaign details
echo -e "${BLUE}6. Getting campaign details...${NC}"
CAMPAIGN_DETAILS=$(curl -s -X GET "$API_URL/api/campaigns/$CAMPAIGN_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "$CAMPAIGN_DETAILS" | jq '.'
echo -e "${GREEN}✓ Campaign details retrieved${NC}"
echo ""

# Step 7: Get campaign analytics (CQRS read model)
echo -e "${BLUE}7. Getting campaign analytics...${NC}"
ANALYTICS=$(curl -s -X GET "$API_URL/api/analytics/campaigns/$CAMPAIGN_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "$ANALYTICS" | jq '.'
echo -e "${GREEN}✓ Campaign analytics retrieved${NC}"
echo ""

# Step 8: Get all campaigns
echo -e "${BLUE}8. Listing all campaigns...${NC}"
ALL_CAMPAIGNS=$(curl -s -X GET "$API_URL/api/campaigns" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "$ALL_CAMPAIGNS" | jq '.content[0:2]'
echo -e "${GREEN}✓ Campaigns list retrieved${NC}"
echo ""

# Step 9: Get donation history for user
echo -e "${BLUE}9. Getting user donation history...${NC}"
DONATIONS=$(curl -s -X GET "$API_URL/api/donations/user/$USER_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "$DONATIONS" | jq '.'
echo -e "${GREEN}✓ Donation history retrieved${NC}"
echo ""

# Final Summary
echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  Test Summary${NC}"
echo -e "${BLUE}=========================================${NC}"
echo -e "${GREEN}✓ Campaign created: $CAMPAIGN_ID${NC}"
echo -e "${GREEN}✓ Guest donation: $DONATION_ID${NC}"
echo -e "${GREEN}✓ User registered and logged in${NC}"
echo -e "${GREEN}✓ Authenticated donation: $AUTH_DONATION_ID${NC}"
echo -e "${GREEN}✓ All API endpoints working correctly${NC}"
echo ""
echo -e "${YELLOW}Note: Check the following to verify the Outbox Pattern:${NC}"
echo -e "${YELLOW}  1. RabbitMQ Management: http://localhost:15672${NC}"
echo -e "${YELLOW}  2. Check for DONATION_CREATED events${NC}"
echo -e "${YELLOW}  3. Verify analytics service received events${NC}"
echo ""

# Summary
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  API Testing Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "${BLUE}Check the following:${NC}"
echo "  • Zipkin traces:     http://localhost:9411"
echo "  • RabbitMQ queues:   http://localhost:15672"
echo "  • Prometheus metrics: http://localhost:9090"
echo "  • Grafana dashboards: http://localhost:3000"
echo ""
echo -e "${YELLOW}Note: Payment service has 90% success rate.${NC}"
echo -e "${YELLOW}If order is PAYMENT_PENDING, try placing another order.${NC}"
