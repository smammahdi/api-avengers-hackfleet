# CareForAll Donation Platform - API Documentation

Complete API reference with sample curl commands for all microservices.

**Base URL (API Gateway)**: `http://localhost:8080`

---

## Table of Contents
1. [Auth Service](#auth-service)
2. [Campaign Service](#campaign-service)
3. [Donation Service](#donation-service)
4. [Payment Service](#payment-service)
5. [Banking Service](#banking-service)
6. [Analytics Service](#analytics-service)
7. [Notification Service](#notification-service)

---

## Auth Service

### Register User
```bash
curl -X POST http://localhost:8080/auth-service/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "fullName": "John Doe"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "USER"
  }
}
```

### Get Current User
```bash
curl -X GET http://localhost:8080/auth-service/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Logout
```bash
curl -X POST http://localhost:8080/auth-service/api/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Campaign Service

### Get All Campaigns
```bash
curl -X GET "http://localhost:8080/campaign-service/api/campaigns?page=0&size=10" \
  -H "Accept: application/json"
```

### Get Campaign by ID
```bash
curl -X GET http://localhost:8080/campaign-service/api/campaigns/1 \
  -H "Accept: application/json"
```

### Get Active Campaigns
```bash
curl -X GET http://localhost:8080/campaign-service/api/campaigns/active \
  -H "Accept: application/json"
```

### Create Campaign (Admin)
```bash
curl -X POST http://localhost:8080/campaign-service/api/campaigns \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Help Children Education",
    "description": "Support education for underprivileged children",
    "targetAmount": 50000.00,
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-12-31T23:59:59",
    "organizerId": 1
  }'
```

### Update Campaign (Admin)
```bash
curl -X PUT http://localhost:8080/campaign-service/api/campaigns/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Help Children Education - Updated",
    "description": "Updated description",
    "targetAmount": 75000.00,
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-12-31T23:59:59"
  }'
```

### Delete Campaign (Admin)
```bash
curl -X DELETE http://localhost:8080/campaign-service/api/campaigns/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Campaign Progress
```bash
curl -X GET http://localhost:8080/campaign-service/api/campaigns/1/progress \
  -H "Accept: application/json"
```

**Response:**
```json
{
  "campaignId": 1,
  "campaignName": "Help Children Education",
  "targetAmount": 50000.00,
  "currentAmount": 25000.00,
  "percentageComplete": 50.0,
  "donationCount": 125,
  "status": "ACTIVE"
}
```

---

## Donation Service

### Create Donation (Guest)
```bash
curl -X POST http://localhost:8080/donation-service/api/donations \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": 1,
    "donorEmail": "donor@example.com",
    "amount": 100.00,
    "isAnonymous": false
  }'
```

### Create Donation (Authenticated User)
```bash
curl -X POST http://localhost:8080/donation-service/api/donations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": 1,
    "userId": 1,
    "amount": 250.00,
    "isAnonymous": false
  }'
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "campaignId": 1,
  "userId": 1,
  "donorEmail": "john@example.com",
  "amount": 250.00,
  "status": "CREATED",
  "isAnonymous": false,
  "createdAt": "2025-11-21T10:30:00",
  "message": "Donation created successfully"
}
```

### Get Donation by ID
```bash
curl -X GET http://localhost:8080/donation-service/api/donations/123e4567-e89b-12d3-a456-426614174000 \
  -H "Accept: application/json"
```

### Get User Donations
```bash
curl -X GET http://localhost:8080/donation-service/api/donations/user/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Donations by Email (for guest donations)
```bash
curl -X GET "http://localhost:8080/donation-service/api/donations/email/donor@example.com" \
  -H "Accept: application/json"
```

### Get Campaign Donations
```bash
curl -X GET http://localhost:8080/donation-service/api/donations/campaign/1 \
  -H "Accept: application/json"
```

### Get All Donations (Admin - with filtering)
```bash
# All donations with pagination
curl -X GET "http://localhost:8080/donation-service/api/donations?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Filter by status
curl -X GET "http://localhost:8080/donation-service/api/donations?page=0&size=20&status=CAPTURED" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Payment Service

### Process Payment
```bash
curl -X POST http://localhost:8080/payment-service/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "donationId": 1,
    "userId": 1,
    "amount": 100.00,
    "paymentMethod": "user@example.com",
    "idempotencyKey": "donation-1-attempt-1",
    "metadata": {
      "campaign": "Help Children Education"
    }
  }'
```

**Response:**
```json
{
  "paymentId": "PAY-123e4567-e89b-12d3-a456-426614174000",
  "idempotencyKey": "donation-1-attempt-1",
  "donationId": 1,
  "userId": 1,
  "amount": 100.00,
  "paymentMethod": "user@example.com",
  "status": "CREATED",
  "message": "Payment created and sent to Banking Service",
  "attemptCount": 0,
  "fromCache": false,
  "createdAt": "2025-11-21T10:30:00",
  "updatedAt": "2025-11-21T10:30:00"
}
```

### Get Payment by ID
```bash
curl -X GET http://localhost:8080/payment-service/api/payments/PAY-123e4567-e89b-12d3-a456-426614174000 \
  -H "Accept: application/json"
```

### Get Payment by Donation ID
```bash
curl -X GET http://localhost:8080/payment-service/api/payments/donation/1 \
  -H "Accept: application/json"
```

---

## Banking Service

### Create Bank Account
```bash
curl -X POST http://localhost:8080/banking-service/api/banking/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "email": "john@example.com",
    "initialBalance": 1000.00
  }'
```

**Response:**
```json
{
  "id": 1,
  "userId": 1,
  "email": "john@example.com",
  "accountNumber": "ACC-1234567890",
  "balance": 1000.00,
  "availableBalance": 1000.00,
  "lockedBalance": 0.00,
  "status": "ACTIVE",
  "createdAt": "2025-11-21T10:00:00"
}
```

### Get Account Balance
```bash
curl -X GET http://localhost:8080/banking-service/api/banking/accounts/1/balance \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "userId": 1,
  "accountNumber": "ACC-1234567890",
  "balance": 1000.00,
  "availableBalance": 950.00,
  "lockedBalance": 50.00,
  "currency": "USD"
}
```

### Add Funds to Account
```bash
curl -X POST http://localhost:8080/banking-service/api/banking/accounts/1/add-funds \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Funds added successfully",
  "transactionId": "TXN-123e4567-e89b-12d3-a456-426614174000",
  "newBalance": 1500.00
}
```

### Get Account Transactions
```bash
curl -X GET "http://localhost:8080/banking-service/api/banking/accounts/1/transactions?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "transactionId": "TXN-123e4567-e89b-12d3-a456-426614174000",
      "accountId": 1,
      "type": "DEBIT",
      "amount": 100.00,
      "balanceAfter": 900.00,
      "description": "Donation payment",
      "paymentId": "PAY-123e4567-e89b-12d3-a456-426614174000",
      "status": "COMPLETED",
      "createdAt": "2025-11-21T10:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

---

## Analytics Service

### Get Campaign Statistics
```bash
curl -X GET http://localhost:8080/analytics-service/api/analytics/campaigns/1 \
  -H "Accept: application/json"
```

**Response:**
```json
{
  "campaignId": 1,
  "campaignName": "Help Children Education",
  "totalDonations": 125,
  "totalAmount": 25000.00,
  "averageDonation": 200.00,
  "uniqueDonors": 98,
  "targetAmount": 50000.00,
  "percentageComplete": 50.0,
  "donationTrend": [
    {"date": "2025-11-01", "count": 12, "amount": 2400.00},
    {"date": "2025-11-02", "count": 15, "amount": 3000.00}
  ]
}
```

### Get Platform Statistics
```bash
curl -X GET http://localhost:8080/analytics-service/api/analytics/platform/stats \
  -H "Accept: application/json"
```

**Response:**
```json
{
  "totalCampaigns": 25,
  "activeCampaigns": 18,
  "completedCampaigns": 7,
  "totalDonations": 5432,
  "totalAmount": 1250000.00,
  "totalDonors": 3421,
  "averageDonationPerCampaign": 50000.00,
  "topCampaigns": [...]
}
```

### Get User Statistics
```bash
curl -X GET http://localhost:8080/analytics-service/api/analytics/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "userId": 1,
  "totalDonations": 15,
  "totalAmountDonated": 3500.00,
  "averageDonation": 233.33,
  "campaignsSupported": 8,
  "firstDonationDate": "2025-01-15T10:30:00",
  "lastDonationDate": "2025-11-21T10:30:00",
  "favoriteCategories": ["Education", "Healthcare"]
}
```

### Get Top Campaigns
```bash
curl -X GET "http://localhost:8080/analytics-service/api/analytics/campaigns/top?limit=5" \
  -H "Accept: application/json"
```

### Get Recent Donations
```bash
curl -X GET "http://localhost:8080/analytics-service/api/analytics/donations/recent?limit=10" \
  -H "Accept: application/json"
```

**Response:**
```json
[
  {
    "donationId": "123e4567-e89b-12d3-a456-426614174000",
    "campaignName": "Help Children Education",
    "amount": 250.00,
    "donorName": "John Doe",
    "isAnonymous": false,
    "timestamp": "2025-11-21T10:30:00"
  },
  ...
]
```

---

## Notification Service

**Note**: Notification Service is internal and communicates via RabbitMQ. It does not expose REST APIs. Notifications are triggered by events:

### Events Handled:
1. **DONATION_COMPLETED** - Sends donation receipt to donor
2. **CAMPAIGN_CREATED** - Notifies platform admins
3. **CAMPAIGN_COMPLETED** - Notifies campaign organizer with final stats
4. **PAYMENT_FAILED** - Notifies donor with retry instructions

### Example Event Flow:
```
User makes donation
    → Donation Service creates donation (status: CREATED)
    → Publishes DONATION_CREATED event to RabbitMQ
    → Payment Service processes payment
    → Banking Service authorizes and captures funds
    → Payment Service publishes PAYMENT_COMPLETED event
    → Donation Service updates donation (status: CAPTURED)
    → Publishes DONATION_COMPLETED event
    → Notification Service receives event and sends email
```

---

## Complete Donation Flow Example

### Step 1: Register User
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth-service/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "donor123",
    "email": "donor@example.com",
    "password": "password123",
    "fullName": "Jane Donor"
  }' | jq -r '.token')
```

### Step 2: Create Bank Account
```bash
curl -X POST http://localhost:8080/banking-service/api/banking/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "email": "donor@example.com",
    "initialBalance": 1000.00
  }'
```

### Step 3: View Active Campaigns
```bash
curl -X GET http://localhost:8080/campaign-service/api/campaigns/active
```

### Step 4: Make Donation
```bash
DONATION=$(curl -s -X POST http://localhost:8080/donation-service/api/donations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": 1,
    "userId": 1,
    "amount": 100.00,
    "isAnonymous": false
  }')

DONATION_ID=$(echo $DONATION | jq -r '.id')
echo "Donation ID: $DONATION_ID"
```

### Step 5: Check Donation Status
```bash
curl -X GET "http://localhost:8080/donation-service/api/donations/$DONATION_ID"
```

### Step 6: View Transaction History
```bash
curl -X GET "http://localhost:8080/banking-service/api/banking/accounts/1/transactions" \
  -H "Authorization: Bearer $TOKEN"
```

### Step 7: View User Statistics
```bash
curl -X GET http://localhost:8080/analytics-service/api/analytics/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Health Checks

### Check All Services Health
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Campaign Service
curl http://localhost:8082/actuator/health

# Donation Service
curl http://localhost:8085/actuator/health

# Payment Service
curl http://localhost:8086/actuator/health

# Banking Service
curl http://localhost:8091/actuator/health

# Analytics Service
curl http://localhost:8087/actuator/health

# Auth Service
curl http://localhost:8089/actuator/health

# Notification Service
curl http://localhost:8088/actuator/health

# Eureka Server
curl http://localhost:8761/actuator/health
```

---

## Error Responses

### Standard Error Format
```json
{
  "timestamp": "2025-11-21T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid donation amount: must be greater than 0",
  "path": "/donation-service/api/donations"
}
```

### Common HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., duplicate donation attempt)
- `500 Internal Server Error` - Server error

---

## Rate Limiting

API Gateway enforces rate limiting:
- **Authenticated users**: 100 requests/minute
- **Anonymous users**: 20 requests/minute

---

## Testing Tips

### 1. Test with jq for JSON parsing
```bash
curl -s http://localhost:8080/campaign-service/api/campaigns/active | jq '.[] | {id, name, targetAmount}'
```

### 2. Save token for multiple requests
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}' | jq -r '.token')

# Use in subsequent requests
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/donation-service/api/donations/user/1
```

### 3. Test idempotency
```bash
# Send same request twice with same idempotency key
KEY="donation-$(date +%s)"

curl -X POST http://localhost:8080/payment-service/api/payments \
  -H "Content-Type: application/json" \
  -d "{\"donationId\":1,\"amount\":100,\"idempotencyKey\":\"$KEY\"}"

# Second request should return cached result
curl -X POST http://localhost:8080/payment-service/api/payments \
  -H "Content-Type: application/json" \
  -d "{\"donationId\":1,\"amount\":100,\"idempotencyKey\":\"$KEY\"}"
```

---

## Monitoring & Observability

### Zipkin (Distributed Tracing)
```bash
# View traces
open http://localhost:9411
```

### Prometheus (Metrics)
```bash
# View metrics
open http://localhost:9090

# Query donation count
curl 'http://localhost:9090/api/v1/query?query=donation_total'
```

### Grafana (Dashboards)
```bash
# Access dashboards
open http://localhost:3000
# Login: admin/admin
```

### Kibana (Logs)
```bash
# View logs
open http://localhost:5601
```

### RabbitMQ Management
```bash
# View queues and messages
open http://localhost:15672
# Login: guest/guest
```

---

## Quick Test Script

```bash
#!/bin/bash

# Complete end-to-end test
BASE_URL="http://localhost:8080"

echo "1. Register user..."
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/auth-service/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"test123","fullName":"Test User"}')

TOKEN=$(echo $REGISTER_RESPONSE | jq -r '.token')
USER_ID=$(echo $REGISTER_RESPONSE | jq -r '.user.id')

echo "2. Create bank account..."
curl -s -X POST $BASE_URL/banking-service/api/banking/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER_ID,\"email\":\"test@example.com\",\"initialBalance\":1000}"

echo "3. Get active campaigns..."
CAMPAIGNS=$(curl -s $BASE_URL/campaign-service/api/campaigns/active)
CAMPAIGN_ID=$(echo $CAMPAIGNS | jq -r '.[0].id')

echo "4. Make donation..."
DONATION=$(curl -s -X POST $BASE_URL/donation-service/api/donations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"campaignId\":$CAMPAIGN_ID,\"userId\":$USER_ID,\"amount\":50}")

DONATION_ID=$(echo $DONATION | jq -r '.id')

echo "5. Check donation status..."
curl -s $BASE_URL/donation-service/api/donations/$DONATION_ID | jq

echo "6. View transaction history..."
curl -s $BASE_URL/banking-service/api/banking/accounts/$USER_ID/transactions \
  -H "Authorization: Bearer $TOKEN" | jq

echo "✅ Test complete!"
```

Save as `test-api.sh` and run:
```bash
chmod +x test-api.sh
./test-api.sh
```
