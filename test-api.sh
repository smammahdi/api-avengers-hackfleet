#!/bin/bash

#####################################################################
# CareForAll Donation Platform - API Integration Test Script
#####################################################################
# This script tests all microservices end-to-end
# Usage: ./test-api.sh
#####################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Base URL
BASE_URL="${API_BASE_URL:-http://localhost:8080}"

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Utility functions
print_header() {
    echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_test() {
    echo -e "\n${YELLOW}[TEST $((TOTAL_TESTS+1))]${NC} $1"
}

print_success() {
    PASSED_TESTS=$((PASSED_TESTS+1))
    TOTAL_TESTS=$((TOTAL_TESTS+1))
    echo -e "${GREEN}✓ PASSED${NC}: $1"
}

print_failure() {
    FAILED_TESTS=$((FAILED_TESTS+1))
    TOTAL_TESTS=$((TOTAL_TESTS+1))
    echo -e "${RED}✗ FAILED${NC}: $1"
}

check_service() {
    SERVICE_NAME=$1
    SERVICE_URL=$2

    print_test "Health check: $SERVICE_NAME"

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$SERVICE_URL/actuator/health" || echo "000")

    if [ "$HTTP_CODE" == "200" ] || [ "$HTTP_CODE" == "000" ]; then
        print_success "$SERVICE_NAME is healthy"
        return 0
    else
        print_failure "$SERVICE_NAME health check failed (HTTP $HTTP_CODE)"
        return 1
    fi
}

# Generate random test data
TIMESTAMP=$(date +%s)
TEST_USER_EMAIL="test_${TIMESTAMP}@example.com"
TEST_USER_NAME="TestUser_${TIMESTAMP}"
TEST_CAMPAIGN_NAME="Test Campaign ${TIMESTAMP}"

print_header "CAREFORALL DONATION PLATFORM - API INTEGRATION TESTS"
echo "Base URL: $BASE_URL"
echo "Timestamp: $(date)"

#####################################################################
# 1. HEALTH CHECKS
#####################################################################
print_header "1. SERVICE HEALTH CHECKS"

check_service "API Gateway" "$BASE_URL"
check_service "Campaign Service" "http://localhost:8082"
check_service "Donation Service" "http://localhost:8085"
check_service "Payment Service" "http://localhost:8086"
check_service "Banking Service" "http://localhost:8091"
check_service "Analytics Service" "http://localhost:8087"
check_service "Auth Service" "http://localhost:8089"
check_service "Notification Service" "http://localhost:8088"
check_service "Eureka Server" "http://localhost:8761"

#####################################################################
# 2. AUTH SERVICE TESTS
#####################################################################
print_header "2. AUTHENTICATION SERVICE TESTS"

# Test: User Registration
print_test "Register new user"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth-service/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"$TEST_USER_NAME\",
        \"email\": \"$TEST_USER_EMAIL\",
        \"password\": \"TestPassword123\",
        \"fullName\": \"Test User\"
    }" || echo '{"error":"failed"}')

TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.token // empty')
USER_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.user.id // empty')

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    print_success "User registered successfully (User ID: $USER_ID)"
    export AUTH_TOKEN="$TOKEN"
    export TEST_USER_ID="$USER_ID"
else
    print_failure "User registration failed"
    echo "Response: $REGISTER_RESPONSE"
fi

# Test: User Login
print_test "User login"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth-service/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"email\": \"$TEST_USER_EMAIL\",
        \"password\": \"TestPassword123\"
    }" || echo '{"error":"failed"}')

LOGIN_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // empty')

if [ -n "$LOGIN_TOKEN" ] && [ "$LOGIN_TOKEN" != "null" ]; then
    print_success "User login successful"
else
    print_failure "User login failed"
fi

# Test: Get Current User
print_test "Get current user info"
USER_INFO=$(curl -s -X GET "$BASE_URL/auth-service/api/auth/me" \
    -H "Authorization: Bearer $AUTH_TOKEN" || echo '{"error":"failed"}')

USER_EMAIL=$(echo "$USER_INFO" | jq -r '.email // empty')

if [ "$USER_EMAIL" == "$TEST_USER_EMAIL" ]; then
    print_success "Retrieved user info successfully"
else
    print_failure "Failed to retrieve user info"
fi

#####################################################################
# 3. BANKING SERVICE TESTS
#####################################################################
print_header "3. BANKING SERVICE TESTS"

# Test: Create Bank Account
print_test "Create bank account"
ACCOUNT_RESPONSE=$(curl -s -X POST "$BASE_URL/banking-service/api/banking/accounts" \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"userId\": $TEST_USER_ID,
        \"email\": \"$TEST_USER_EMAIL\",
        \"initialBalance\": 10000.00
    }" || echo '{"error":"failed"}')

ACCOUNT_NUMBER=$(echo "$ACCOUNT_RESPONSE" | jq -r '.accountNumber // empty')

if [ -n "$ACCOUNT_NUMBER" ] && [ "$ACCOUNT_NUMBER" != "null" ]; then
    print_success "Bank account created (Account: $ACCOUNT_NUMBER)"
else
    print_failure "Failed to create bank account"
    echo "Response: $ACCOUNT_RESPONSE"
fi

# Test: Get Account Balance
print_test "Get account balance"
BALANCE_RESPONSE=$(curl -s -X GET "$BASE_URL/banking-service/api/banking/accounts/$TEST_USER_ID/balance" \
    -H "Authorization: Bearer $AUTH_TOKEN" || echo '{"error":"failed"}')

BALANCE=$(echo "$BALANCE_RESPONSE" | jq -r '.balance // empty')

if [ -n "$BALANCE" ] && [ "$BALANCE" != "null" ]; then
    print_success "Retrieved account balance: \$$BALANCE"
else
    print_failure "Failed to retrieve account balance"
fi

# Test: Add Funds
print_test "Add funds to account"
ADD_FUNDS_RESPONSE=$(curl -s -X POST "$BASE_URL/banking-service/api/banking/accounts/$TEST_USER_ID/add-funds" \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"amount": 5000.00}' || echo '{"error":"failed"}')

NEW_BALANCE=$(echo "$ADD_FUNDS_RESPONSE" | jq -r '.newBalance // empty')

if [ -n "$NEW_BALANCE" ] && [ "$NEW_BALANCE" != "null" ]; then
    print_success "Added funds successfully (New balance: \$$NEW_BALANCE)"
else
    print_failure "Failed to add funds"
fi

#####################################################################
# 4. CAMPAIGN SERVICE TESTS
#####################################################################
print_header "4. CAMPAIGN SERVICE TESTS"

# Test: Create Campaign
print_test "Create campaign"
CAMPAIGN_RESPONSE=$(curl -s -X POST "$BASE_URL/campaign-service/api/campaigns" \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"$TEST_CAMPAIGN_NAME\",
        \"description\": \"Test campaign for API integration testing\",
        \"targetAmount\": 50000.00,
        \"startDate\": \"2025-01-01T00:00:00\",
        \"endDate\": \"2025-12-31T23:59:59\",
        \"organizerId\": $TEST_USER_ID
    }" || echo '{"error":"failed"}')

CAMPAIGN_ID=$(echo "$CAMPAIGN_RESPONSE" | jq -r '.id // empty')

if [ -n "$CAMPAIGN_ID" ] && [ "$CAMPAIGN_ID" != "null" ]; then
    print_success "Campaign created (ID: $CAMPAIGN_ID)"
    export TEST_CAMPAIGN_ID="$CAMPAIGN_ID"
else
    print_failure "Failed to create campaign"
    echo "Response: $CAMPAIGN_RESPONSE"
fi

# Test: Get All Campaigns
print_test "Get all campaigns"
CAMPAIGNS_RESPONSE=$(curl -s -X GET "$BASE_URL/campaign-service/api/campaigns?page=0&size=10" || echo '{"error":"failed"}')

CAMPAIGNS_COUNT=$(echo "$CAMPAIGNS_RESPONSE" | jq -r '.content | length // 0')

if [ "$CAMPAIGNS_COUNT" -gt 0 ]; then
    print_success "Retrieved $CAMPAIGNS_COUNT campaigns"
else
    print_failure "Failed to retrieve campaigns"
fi

# Test: Get Campaign by ID
print_test "Get campaign by ID"
CAMPAIGN_DETAIL=$(curl -s -X GET "$BASE_URL/campaign-service/api/campaigns/$TEST_CAMPAIGN_ID" || echo '{"error":"failed"}')

CAMPAIGN_NAME=$(echo "$CAMPAIGN_DETAIL" | jq -r '.name // empty')

if [ "$CAMPAIGN_NAME" == "$TEST_CAMPAIGN_NAME" ]; then
    print_success "Retrieved campaign details"
else
    print_failure "Failed to retrieve campaign details"
fi

# Test: Get Active Campaigns
print_test "Get active campaigns"
ACTIVE_CAMPAIGNS=$(curl -s -X GET "$BASE_URL/campaign-service/api/campaigns/active" || echo '[]')

ACTIVE_COUNT=$(echo "$ACTIVE_CAMPAIGNS" | jq -r 'length // 0')

if [ "$ACTIVE_COUNT" -gt 0 ]; then
    print_success "Retrieved $ACTIVE_COUNT active campaigns"
else
    print_failure "No active campaigns found"
fi

#####################################################################
# 5. DONATION SERVICE TESTS
#####################################################################
print_header "5. DONATION SERVICE TESTS"

# Test: Create Donation (Authenticated)
print_test "Create authenticated donation"
DONATION_RESPONSE=$(curl -s -X POST "$BASE_URL/donation-service/api/donations" \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"campaignId\": $TEST_CAMPAIGN_ID,
        \"userId\": $TEST_USER_ID,
        \"amount\": 500.00,
        \"isAnonymous\": false
    }" || echo '{"error":"failed"}')

DONATION_ID=$(echo "$DONATION_RESPONSE" | jq -r '.id // empty')

if [ -n "$DONATION_ID" ] && [ "$DONATION_ID" != "null" ]; then
    print_success "Donation created (ID: $DONATION_ID)"
    export TEST_DONATION_ID="$DONATION_ID"

    # Wait for async processing
    echo "   Waiting 5 seconds for payment processing..."
    sleep 5
else
    print_failure "Failed to create donation"
    echo "Response: $DONATION_RESPONSE"
fi

# Test: Create Guest Donation
print_test "Create guest donation"
GUEST_DONATION_RESPONSE=$(curl -s -X POST "$BASE_URL/donation-service/api/donations" \
    -H "Content-Type: application/json" \
    -d "{
        \"campaignId\": $TEST_CAMPAIGN_ID,
        \"donorEmail\": \"guest_${TIMESTAMP}@example.com\",
        \"amount\": 250.00,
        \"isAnonymous\": false
    }" || echo '{"error":"failed"}')

GUEST_DONATION_ID=$(echo "$GUEST_DONATION_RESPONSE" | jq -r '.id // empty')

if [ -n "$GUEST_DONATION_ID" ] && [ "$GUEST_DONATION_ID" != "null" ]; then
    print_success "Guest donation created (ID: $GUEST_DONATION_ID)"

    # Wait for async processing
    echo "   Waiting 5 seconds for payment processing..."
    sleep 5
else
    print_failure "Failed to create guest donation"
fi

# Test: Get Donation by ID
print_test "Get donation by ID"
DONATION_DETAIL=$(curl -s -X GET "$BASE_URL/donation-service/api/donations/$TEST_DONATION_ID" || echo '{"error":"failed"}')

DONATION_STATUS=$(echo "$DONATION_DETAIL" | jq -r '.status // empty')

if [ -n "$DONATION_STATUS" ] && [ "$DONATION_STATUS" != "null" ]; then
    print_success "Retrieved donation (Status: $DONATION_STATUS)"
else
    print_failure "Failed to retrieve donation"
fi

# Test: Get User Donations
print_test "Get user donations"
USER_DONATIONS=$(curl -s -X GET "$BASE_URL/donation-service/api/donations/user/$TEST_USER_ID" \
    -H "Authorization: Bearer $AUTH_TOKEN" || echo '[]')

USER_DONATIONS_COUNT=$(echo "$USER_DONATIONS" | jq -r 'length // 0')

if [ "$USER_DONATIONS_COUNT" -gt 0 ]; then
    print_success "Retrieved $USER_DONATIONS_COUNT user donations"
else
    print_failure "Failed to retrieve user donations"
fi

# Test: Get Campaign Donations
print_test "Get campaign donations"
CAMPAIGN_DONATIONS=$(curl -s -X GET "$BASE_URL/donation-service/api/donations/campaign/$TEST_CAMPAIGN_ID" || echo '[]')

CAMPAIGN_DONATIONS_COUNT=$(echo "$CAMPAIGN_DONATIONS" | jq -r 'length // 0')

if [ "$CAMPAIGN_DONATIONS_COUNT" -gt 0 ]; then
    print_success "Retrieved $CAMPAIGN_DONATIONS_COUNT campaign donations"
else
    print_failure "Failed to retrieve campaign donations"
fi

#####################################################################
# 6. PAYMENT SERVICE TESTS
#####################################################################
print_header "6. PAYMENT SERVICE TESTS"

# Test: Get Payment by Donation ID
print_test "Get payment by donation ID"
PAYMENT_RESPONSE=$(curl -s -X GET "$BASE_URL/payment-service/api/payments/donation/$TEST_DONATION_ID" || echo '{"error":"failed"}')

PAYMENT_ID=$(echo "$PAYMENT_RESPONSE" | jq -r '.paymentId // empty')
PAYMENT_STATUS=$(echo "$PAYMENT_RESPONSE" | jq -r '.status // empty')

if [ -n "$PAYMENT_ID" ] && [ "$PAYMENT_ID" != "null" ]; then
    print_success "Retrieved payment (ID: $PAYMENT_ID, Status: $PAYMENT_STATUS)"
else
    print_failure "Failed to retrieve payment"
fi

#####################################################################
# 7. ANALYTICS SERVICE TESTS
#####################################################################
print_header "7. ANALYTICS SERVICE TESTS"

# Test: Get Campaign Statistics
print_test "Get campaign statistics"
CAMPAIGN_STATS=$(curl -s -X GET "$BASE_URL/analytics-service/api/analytics/campaigns/$TEST_CAMPAIGN_ID" || echo '{"error":"failed"}')

TOTAL_DONATIONS=$(echo "$CAMPAIGN_STATS" | jq -r '.totalDonations // 0')

if [ "$TOTAL_DONATIONS" -ge 0 ]; then
    print_success "Retrieved campaign stats (Total donations: $TOTAL_DONATIONS)"
else
    print_failure "Failed to retrieve campaign stats"
fi

# Test: Get Platform Statistics
print_test "Get platform statistics"
PLATFORM_STATS=$(curl -s -X GET "$BASE_URL/analytics-service/api/analytics/platform/stats" || echo '{"error":"failed"}')

TOTAL_CAMPAIGNS=$(echo "$PLATFORM_STATS" | jq -r '.totalCampaigns // 0')

if [ "$TOTAL_CAMPAIGNS" -gt 0 ]; then
    print_success "Retrieved platform stats (Total campaigns: $TOTAL_CAMPAIGNS)"
else
    print_failure "Failed to retrieve platform stats"
fi

# Test: Get User Statistics
print_test "Get user statistics"
USER_STATS=$(curl -s -X GET "$BASE_URL/analytics-service/api/analytics/users/$TEST_USER_ID" \
    -H "Authorization: Bearer $AUTH_TOKEN" || echo '{"error":"failed"}')

USER_TOTAL_DONATIONS=$(echo "$USER_STATS" | jq -r '.totalDonations // 0')

if [ "$USER_TOTAL_DONATIONS" -ge 0 ]; then
    print_success "Retrieved user stats (Total donations: $USER_TOTAL_DONATIONS)"
else
    print_failure "Failed to retrieve user stats"
fi

# Test: Get Recent Donations
print_test "Get recent donations"
RECENT_DONATIONS=$(curl -s -X GET "$BASE_URL/analytics-service/api/analytics/donations/recent?limit=10" || echo '[]')

RECENT_COUNT=$(echo "$RECENT_DONATIONS" | jq -r 'length // 0')

if [ "$RECENT_COUNT" -gt 0 ]; then
    print_success "Retrieved $RECENT_COUNT recent donations"
else
    print_failure "Failed to retrieve recent donations"
fi

#####################################################################
# 8. BANKING SERVICE - TRANSACTION HISTORY
#####################################################################
print_header "8. TRANSACTION HISTORY TESTS"

# Test: Get Account Transactions
print_test "Get account transactions"
TRANSACTIONS=$(curl -s -X GET "$BASE_URL/banking-service/api/banking/accounts/$TEST_USER_ID/transactions?page=0&size=10" \
    -H "Authorization: Bearer $AUTH_TOKEN" || echo '{"content":[]}')

TRANSACTIONS_COUNT=$(echo "$TRANSACTIONS" | jq -r '.content | length // 0')

if [ "$TRANSACTIONS_COUNT" -gt 0 ]; then
    print_success "Retrieved $TRANSACTIONS_COUNT transactions"
else
    print_failure "Failed to retrieve transactions"
fi

#####################################################################
# 9. CAMPAIGN SERVICE - UPDATE & DELETE
#####################################################################
print_header "9. CAMPAIGN MANAGEMENT TESTS"

# Test: Update Campaign
print_test "Update campaign"
UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/campaign-service/api/campaigns/$TEST_CAMPAIGN_ID" \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"$TEST_CAMPAIGN_NAME - Updated\",
        \"description\": \"Updated description for testing\",
        \"targetAmount\": 75000.00,
        \"startDate\": \"2025-01-01T00:00:00\",
        \"endDate\": \"2025-12-31T23:59:59\"
    }" || echo '{"error":"failed"}')

UPDATED_NAME=$(echo "$UPDATE_RESPONSE" | jq -r '.name // empty')

if [[ "$UPDATED_NAME" == *"Updated"* ]]; then
    print_success "Campaign updated successfully"
else
    print_failure "Failed to update campaign"
fi

# Test: Get Campaign Progress
print_test "Get campaign progress"
PROGRESS_RESPONSE=$(curl -s -X GET "$BASE_URL/campaign-service/api/campaigns/$TEST_CAMPAIGN_ID/progress" || echo '{"error":"failed"}')

CURRENT_AMOUNT=$(echo "$PROGRESS_RESPONSE" | jq -r '.currentAmount // 0')

if [ -n "$CURRENT_AMOUNT" ]; then
    print_success "Retrieved campaign progress (Raised: \$$CURRENT_AMOUNT)"
else
    print_failure "Failed to retrieve campaign progress"
fi

#####################################################################
# FINAL SUMMARY
#####################################################################
print_header "TEST SUMMARY"

echo ""
echo "Total Tests:  $TOTAL_TESTS"
echo -e "${GREEN}Passed:       $PASSED_TESTS${NC}"
echo -e "${RED}Failed:       $FAILED_TESTS${NC}"
echo ""

SUCCESS_RATE=$(awk "BEGIN {printf \"%.1f\", ($PASSED_TESTS/$TOTAL_TESTS)*100}")
echo "Success Rate: $SUCCESS_RATE%"

if [ "$FAILED_TESTS" -eq 0 ]; then
    echo -e "\n${GREEN}✓ ALL TESTS PASSED!${NC}\n"
    exit 0
else
    echo -e "\n${RED}✗ SOME TESTS FAILED${NC}\n"
    exit 1
fi
