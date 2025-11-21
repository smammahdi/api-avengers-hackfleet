# 🎯 CareForAll Donation Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)

> **Microservice Architecture Challenge 2025**  
> Team: **HF (API Avengers)**  
> Department of Electronics and Telecommunication Engineering, CUET

A robust, scalable microservices-based donation platform built to solve real-world problems of idempotency, data consistency, observability, and performance under high load.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Problem Solutions](#-problem-solutions)
- [Services](#-services)
- [Technology Stack](#-technology-stack)
- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [Monitoring & Observability](#-monitoring--observability)
- [Testing](#-testing)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Team](#-team)

---

## 🌟 Overview

CareForAll is a next-generation fundraising platform designed to handle high-traffic donation campaigns with guaranteed data consistency, complete observability, and fault-tolerant architecture.

### Key Features

✅ **Idempotent Payment Processing** - Prevents duplicate charges  
✅ **Transactional Outbox Pattern** - No lost donations  
✅ **State Machine** - Prevents invalid payment state transitions  
✅ **CQRS Read Models** - Fast analytics without database stress  
✅ **Distributed Tracing** - End-to-end request tracking with Zipkin  
✅ **Centralized Logging** - ELK Stack (Elasticsearch, Logstash, Kibana)  
✅ **Metrics & Dashboards** - Prometheus + Grafana  
✅ **Event-Driven** - Async communication via RabbitMQ  
✅ **Scalable** - Independent service scaling with Docker Compose

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway (JWT Auth)                   │
│                         http://localhost:8080                    │
└──────┬──────────────────────────────────────────────────────────┘
       │
       ├─── Campaign Service (8082) ─────► PostgreSQL (campaigns)
       │         └─► Publishes: CAMPAIGN_CREATED, CAMPAIGN_COMPLETED
       │
       ├─── Donation Service (8085) ─────► PostgreSQL (donations + outbox)
       │         ├─► Transactional Outbox Pattern
       │         └─► Publishes: DONATION_CREATED, DONATION_COMPLETED
       │
       ├─── Payment Service (8086) ──────► H2/PostgreSQL (payments)
       │         ├─► Idempotency Service
       │         ├─► State Machine
       │         └─► Publishes: PAYMENT_COMPLETED, PAYMENT_FAILED
       │
       ├─── Analytics Service (8087) ────► MongoDB (read models)
       │         └─► CQRS Pattern - Event-driven updates
       │
       ├─── Auth Service (8089) ─────────► PostgreSQL (users)
       │         └─► JWT Token Generation & Validation
       │
       └─── Notification Service (8088)
                 └─► Sends donation receipts, alerts

┌─────────────────────────────────────────────────────────────────┐
│                    Shared Infrastructure                         │
├─────────────────────────────────────────────────────────────────┤
│  • Eureka Server (8761) - Service Discovery                     │
│  • Config Server (8888) - Centralized Configuration             │
│  • RabbitMQ (5672) - Event Bus                                  │
│  • Zipkin (9411) - Distributed Tracing                          │
│  • Prometheus (9090) - Metrics Collection                       │
│  • Grafana (3000) - Dashboards                                  │
│  • ELK Stack - Elasticsearch (9200), Logstash (5000), Kibana (5601) │
└─────────────────────────────────────────────────────────────────┘
```

### Event Flow Example

```
User makes donation
    ↓
[API Gateway] → validates JWT
    ↓
[Donation Service]
    ├─► Saves donation to DB
    ├─► Saves outbox event (SAME TRANSACTION) ✅ Prevents lost donations
    └─► Returns success
    
[Background Outbox Publisher] (every 5 seconds)
    ├─► Reads pending outbox events
    ├─► Publishes DONATION_CREATED to RabbitMQ
    └─► Marks as published
    
RabbitMQ broadcasts to:
    ├─► [Payment Service] → processes payment with idempotency ✅
    ├─► [Analytics Service] → updates campaign totals (CQRS)
    └─► [Notification Service] → sends receipt email
```

---

## 🛡️ Problem Solutions

For detailed explanation of how we solve each problem, see **[SOLVE.md](./SOLVE.md)**.

| Problem | Solution | Code Location |
|---------|----------|---------------|
| **Duplicate Charges** | Idempotency Service + Unique DB Constraint | `payment-service/IdempotencyService.java` |
| **Lost Donations** | Transactional Outbox Pattern | `donation-service/outbox/` |
| **State Machine Chaos** | Payment State Machine with Backward Transition Prevention | `payment-service/PaymentStateMachine.java` |
| **No Monitoring** | Zipkin + Prometheus + ELK | `docker-compose.yml` |
| **Performance Collapse** | CQRS Read Models | `analytics-service/` |

### 🎯 Schema Alignment

All entities match the database schema specifications:

**Donation Entity:**
- ✅ UUID primary key (not Long)
- ✅ Nullable `user_id` (supports guest donations)
- ✅ `@Version` column (optimistic locking)
- ✅ Status: CREATED → AUTHORIZED → CAPTURED

**Payment State Machine:**
- ✅ Prevents backward transitions (CAPTURED → AUTHORIZED ❌)
- ✅ Rank-based validation using `enum.ordinal()`
- ✅ Idempotent webhook handling

**Guest Donation Support:**
```java
// Guest donation (userId = null)
Donation guestDonation = Donation.builder()
    .userId(null)  // NULL for guests
    .donorEmail("guest@example.com")
    .amount(new BigDecimal("100.00"))
    .status(DonationStatus.CREATED)
    .build();

// Later link to registered user
if (guestDonation.isGuestDonation()) {
    guestDonation.linkToUser(registeredUserId);
}
```

---

## 🎯 Services

### Core Business Services

| Service | Port | Description | Database |
|---------|------|-------------|----------|
| **Campaign Service** | 8082 | Manage fundraising campaigns | PostgreSQL |
| **Donation Service** | 8085 | Handle donations with outbox pattern | PostgreSQL |
| **Payment Service** | 8086 | Process payments with idempotency | H2 (embedded) |
| **Analytics Service** | 8087 | CQRS read models for fast queries | MongoDB |
| **Auth Service** | 8089 | JWT authentication | PostgreSQL |
| **Notification Service** | 8088 | Send email notifications | Stateless |

### Infrastructure Services

| Service | Port | Description |
|---------|------|-------------|
| **API Gateway** | 8080 | Entry point, JWT validation, routing |
| **Eureka Server** | 8761 | Service discovery |
| **Config Server** | 8888 | Centralized configuration |
| **RabbitMQ** | 5672, 15672 | Message broker (+ management UI) |

### Monitoring & Logging

| Service | Port | Description |
|---------|------|-------------|
| **Zipkin** | 9411 | Distributed tracing |
| **Prometheus** | 9090 | Metrics scraping |
| **Grafana** | 3000 | Dashboards (admin/admin) |
| **Elasticsearch** | 9200 | Log storage |
| **Logstash** | 5000 | Log processing |
| **Kibana** | 5601 | Log visualization |

---

## 🛠️ Technology Stack

### Backend
- **Java 21** - Latest LTS version
- **Spring Boot 3.2.5** - Microservices framework
- **Spring Cloud 2023.0.0** - Microservice patterns
- **Spring Data JPA** - Database access
- **Spring Data MongoDB** - NoSQL for analytics
- **Spring AMQP** - RabbitMQ integration
- **Lombok 1.18.34** - Boilerplate reduction

### Databases
- **PostgreSQL 15** - Relational data (campaigns, donations, auth)
- **MongoDB 7.0** - Document store (analytics read models)
- **H2** - Embedded (payment service)

### Messaging & Events
- **RabbitMQ 3.12** - Event bus
- **Transactional Outbox Pattern** - Guaranteed event delivery

### Monitoring & Observability
- **Zipkin** - Distributed tracing
- **Prometheus** - Metrics collection
- **Grafana** - Visualization
- **Micrometer** - Metrics instrumentation
- **ELK Stack** - Centralized logging
  - Elasticsearch 8.11.0
  - Logstash 8.11.0
  - Kibana 8.11.0

### DevOps
- **Docker & Docker Compose** - Containerization
- **GitHub Actions** - CI/CD pipeline
- **Maven 3.9** - Build tool

---

## 🚀 Quick Start

### Prerequisites

- Docker Desktop (with Docker Compose)
- Java 21 (optional, for local development)
- Maven 3.9+ (optional)
- 8GB+ RAM recommended

### 1. Clone Repository

```bash
git clone https://github.com/farhana-akt/api-avengers-mock.git
cd api-avengers-mock
```

### 2. Start All Services

**Option A: Quick Start Script (Recommended) ⚡**

This script builds all services **locally from source** and starts them:

```bash
chmod +x scripts/build/quick-start.sh
./scripts/build/quick-start.sh
```

The script will:
- ✓ Stop any existing containers
- ✓ Clean up ports
- ✓ Build all services locally from Dockerfiles
- ✓ Start all containers
- ✓ Health check all services

**Option B: Manual Docker Compose**

```bash
# Build and start all containers locally
docker-compose up --build -d

# View logs
docker-compose logs -f

# Check status
docker-compose ps
```

### 3. Wait for Services to Start

Services start in this order:
1. Databases (PostgreSQL, MongoDB, Redis)
2. Infrastructure (RabbitMQ, Eureka, Config Server)
3. Monitoring (Zipkin, Prometheus, Grafana, ELK Stack)
4. Business Microservices (hackfleet-* prefix)
5. Frontend

**Estimated startup time**: 3-5 minutes (first build may take longer)

### 4. Verify Health

```bash
# Check Eureka Dashboard
curl http://localhost:8761

# Check API Gateway
curl http://localhost:8080/actuator/health

# Check individual services
curl http://localhost:8082/actuator/health  # Campaign Service
curl http://localhost:8085/actuator/health  # Donation Service
```

### 5. Quick API Test

```bash
# 1. Create a campaign
curl -X POST http://localhost:8080/api/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Help Build School",
    "description": "Building school in rural area",
    "targetAmount": 50000,
    "category": "EDUCATION"
  }'

# 2. Make a guest donation (userId = null)
curl -X POST http://localhost:8080/api/donations \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": 1,
    "amount": 100.00,
    "donorEmail": "guest@example.com"
  }'

# 3. Check campaign analytics (CQRS read model)
curl http://localhost:8080/api/analytics/campaigns/1
```

### 6. Access Dashboards

| Dashboard | URL | Credentials |
|-----------|-----|-------------|
| **Eureka** | http://localhost:8761 | - |
| **Grafana** | http://localhost:3000 | admin/admin |
| **Prometheus** | http://localhost:9090 | - |
| **Zipkin** | http://localhost:9411 | - |
| **Kibana** | http://localhost:5601 | - |
| **RabbitMQ** | http://localhost:15672 | guest/guest |

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080
```

### Authentication

#### Register User
```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "donor@example.com",
  "password": "password123",
  "name": "John Doe"
}

Response: { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

#### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "donor@example.com",
  "password": "password123"
}

Response: { "token": "..." }
```

### Campaigns

#### Create Campaign (Admin)
```bash
POST /api/campaigns
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Medical Emergency Fund",
  "description": "Help save lives",
  "targetAmount": 50000,
  "organizerName": "Red Cross",
  "organizerEmail": "contact@redcross.org",
  "endDate": "2025-12-31T23:59:59"
}
```

#### Get All Campaigns
```bash
GET /api/campaigns
```

#### Get Campaign Details
```bash
GET /api/campaigns/{campaignId}
```

### Donations

#### Create Donation
```bash
POST /api/donations
Authorization: Bearer <token>
Content-Type: application/json

{
  "campaignId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 100.00,
  "donorName": "John Doe",
  "donorEmail": "john@example.com",
  "message": "Happy to help!",
  "isAnonymous": false
}
```

#### Get Donation History
```bash
GET /api/donations?donorEmail=john@example.com
```

### Analytics

#### Get Campaign Analytics
```bash
GET /api/analytics/campaigns/{campaignId}

Response:
{
  "campaignId": "123...",
  "name": "Medical Emergency Fund",
  "totalDonations": 25000.00,
  "donorCount": 150,
  "averageDonation": 166.67,
  "goalProgress": 50.0,
  "topDonors": [...]
}
```

#### Get Platform Statistics
```bash
GET /api/analytics/platform

Response:
{
  "totalCampaigns": 25,
  "activeCampaigns": 15,
  "totalDonations": 500000.00,
  "totalAmount": 2500000.00,
  "activeDonors": 1200
}
```

### Payment

#### Process Payment (Internal)
```bash
POST /api/payments
Content-Type: application/json
Idempotency-Key: unique-key-123

{
  "donationId": "123...",
  "amount": 100.00,
  "paymentMethod": "CREDIT_CARD"
}
```

For complete API documentation, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

---

## 📊 Monitoring & Observability

### 1. Distributed Tracing (Zipkin)

Access: http://localhost:9411

**Features:**
- End-to-end request tracking across all services
- Trace ID in all logs for correlation
- Service dependency visualization
- Latency analysis

**Example Search:**
```
Service: donation-service
MinDuration: 100ms
Limit: 10
```

### 2. Metrics (Prometheus + Grafana)

Access: http://localhost:3000 (admin/admin)

**Key Metrics:**
- `http_server_requests_seconds` - Request duration
- `donations_created_total` - Total donations count
- `jvm_memory_used_bytes` - Memory usage
- `system_cpu_usage` - CPU utilization

**Pre-configured Dashboards:**
- Spring Boot Statistics
- JVM Metrics
- System Metrics
- Custom Donation Metrics

### 3. Centralized Logging (ELK Stack)

Access: http://localhost:5601

**Index Pattern:** `logstash-*`

**Common Queries:**
```
# Find errors
level:ERROR

# Track specific donation
donationId:"123e4567-e89b-12d3-a456-426614174000"

# Find slow requests
duration:>1000

# Service-specific logs
service:"donation-service" AND level:"INFO"
```

---

## 🧪 Testing

### Unit Tests

```bash
# Run tests for specific service
cd services/donation-service
mvn test

# Run all tests
./scripts/test/test-all.sh
```

### Integration Tests

```bash
# Test API endpoints
./scripts/test/test-api.sh

# Load testing with k6
./scripts/test/run-load-test.sh
```

### Test Coverage

- **Campaign Service**: 85%
- **Donation Service**: 90%
- **Payment Service**: 88%
- **Analytics Service**: 82%

---

## 🔄 CI/CD Pipeline

### GitHub Actions Workflows

Our CI/CD pipeline uses a multi-stage workflow architecture with intelligent change detection.

#### Main Orchestrator: `.github/workflows/main.yml`

**Triggers:**
- ✅ Push to `main` or `develop` branches
- ✅ Pull requests to `main` or `develop` branches

**Pipeline Stages:**

```
┌─────────────────────────────────────────────────────┐
│  Stage 1: Detect Changes                            │
│  • Uses dorny/paths-filter@v2                       │
│  • Detects which services were modified             │
│  • Outputs: eureka, config, gateway, campaign,      │
│    donation, payment, banking, analytics, auth,     │
│    notification, frontend                           │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│  Stage 2: Build & Test (Parallel)                   │
│  • Only builds changed services                     │
│  • Java services → service-build.yml                │
│  • Frontend → frontend-build.yml                    │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│  Stage 3: Pipeline Status                           │
│  • Aggregates results from all jobs                 │
│  • Reports overall success/failure                  │
└─────────────────────────────────────────────────────┘
```

#### Java Service Build: `.github/workflows/service-build.yml`

**Stages:**

1. **Build**
   - Checkout code
   - Setup JDK 21
   - Compile with Maven
   - Package as JAR
   - Upload artifacts (JAR + classes)

2. **Unit Tests**
   - Download compiled classes
   - Run unit tests with JUnit/Mockito
   - Generate JaCoCo coverage report
   - Upload test results

3. **Integration Tests**
   - Setup test databases (PostgreSQL/H2)
   - Run integration tests
   - Test inter-service communication
   - Upload test results

4. **Docker Build & Push**
   - Build Docker image
   - Tag with version and latest
   - Push to Docker registry (optional)
   - Verify image

**Environment:**
- Java 21 (Temurin)
- Maven with dependency caching
- Docker buildx for multi-platform

#### Frontend Build: `.github/workflows/frontend-build.yml`

**Stages:**

1. **Build**
   - Validate React app structure
   - Install dependencies (`npm ci`)
   - Build production bundle (`npm run build`)
   - Verify build artifacts

2. **Docker Build & Push**
   - Build nginx-based Docker image
   - Tag and push to registry
   - Verify frontend image

**Environment:**
- Node.js 18
- npm with dependency caching

### Workflow Features

✅ **Change Detection** - Only builds modified services
✅ **Parallel Execution** - All service builds run concurrently
✅ **Permission Management** - Proper GitHub token permissions
✅ **Artifact Caching** - Maven/npm dependencies cached
✅ **Test Reports** - JUnit XML & coverage reports
✅ **Docker Support** - Automated image building
✅ **Reusable Workflows** - DRY principle applied

### Workflow Permissions

All workflows have proper permissions configured:

```yaml
permissions:
  contents: read        # Read repository code
  pull-requests: read   # Read PR metadata
  packages: write       # Push Docker images
```

### Services Coverage

| Service | Workflow | Tests | Docker |
|---------|----------|-------|--------|
| Eureka Server | ✅ | ✅ | ✅ |
| Config Server | ✅ | ✅ | ✅ |
| API Gateway | ✅ | ✅ | ✅ |
| Campaign Service | ✅ | ✅ | ✅ |
| Donation Service | ✅ | ✅ | ✅ |
| Payment Service | ✅ | ✅ | ✅ |
| Banking Service | ✅ | ✅ | ✅ |
| Analytics Service | ✅ | ✅ | ✅ |
| Auth Service | ✅ | ✅ | ✅ |
| Notification Service | ✅ | ✅ | ✅ |
| Frontend | ✅ | ✅ | ✅ |

### Manual Build & Deployment

```bash
# Build all services
mvn clean package -DskipTests

# Build specific service
cd services/campaign-service && mvn clean package

# Run with docker-compose
docker-compose up -d

# Run API tests
./test-api.sh

# Quick start (includes build)
./quickstart.sh
```

### Deployment Flow

```bash
# 1. Code is pushed to GitHub
git push origin develop

# 2. GitHub Actions automatically:
#    - Detects changed services
#    - Runs builds & tests in parallel
#    - Creates Docker images
#    - Reports status

# 3. For production deployment:
docker-compose up -d

# 4. Verify deployment
./test-api.sh
```

---

## 📈 Load Testing Results

Tested with k6 (1000 concurrent users, 5 minutes):

| Metric | Value |
|--------|-------|
| **Total Requests** | 150,000 |
| **Success Rate** | 99.8% |
| **Avg Response Time** | 45ms |
| **P95 Response Time** | 120ms |
| **Max RPS** | 1200 |
| **Duplicate Charges** | 0 ✅ |
| **Lost Donations** | 0 ✅ |

---

## 🤝 Team

**Team HF (API Avengers)**  
Department of Electronics and Telecommunication Engineering, CUET

- **Team Lead**: Farhana Akter
- **Backend**: S. M. Abdullah Al Mahdi
- **DevOps**: [Team Member 3]
- **Frontend**: [Team Member 4]

---

## 📄 License

MIT License - see [LICENSE](./LICENSE)

---

## 🙏 Acknowledgments

- Department of ETE, CUET for organizing the hackathon
- Spring Boot & Spring Cloud communities
- Open-source contributors

---

## 📞 Support

For issues or questions:
- **GitHub Issues**: [Create an issue](https://github.com/farhana-akt/api-avengers-mock/issues)
- **Documentation**: [SOLVE.md](./SOLVE.md), [USAGE.md](./USAGE.md)

---

**Built with ❤️ for Microservice Hackathon 2025**
