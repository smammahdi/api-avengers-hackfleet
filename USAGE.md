# CareForAll Donation Platform - Usage Guide

Complete guide for building, running, testing, and monitoring the CareForAll Donation Platform.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Build & Run](#build--run)
- [Service Endpoints](#service-endpoints)
- [API Examples](#api-examples)
- [Testing](#testing)
- [Monitoring & Observability](#monitoring--observability)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

1. **Docker Desktop** (version 20.10+)
   - Download: https://www.docker.com/products/docker-desktop
   - Ensure Docker is running before starting services

2. **Java 21** (for local development)
   ```bash
   java -version  # Should show Java 21
   ```

3. **Maven 3.9+** (for building services)
   ```bash
   mvn -version
   ```

4. **Git** (for cloning repository)

### System Requirements

- **RAM**: Minimum 8GB, Recommended 16GB
- **CPU**: 4+ cores recommended
- **Disk Space**: 10GB+ free space
- **OS**: Linux, macOS, or Windows 10/11

### Port Requirements

Ensure the following ports are available:

| Ports | Services |
|-------|----------|
| 8080-8090 | Microservices (API Gateway, Campaign, Donation, Payment, etc.) |
| 8761 | Eureka Server |
| 8888 | Config Server |
| 5432-5435 | PostgreSQL databases |
| 27017 | MongoDB |
| 5672, 15672 | RabbitMQ |
| 9090 | Prometheus |
| 3000 | Grafana |
| 9411 | Zipkin |
| 9200 | Elasticsearch |
| 5000 | Logstash |
| 5601 | Kibana |

Check port availability:
```bash
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows
```

---

## Quick Start

### One-Command Start

```bash
# Clone repository
git clone https://github.com/farhana-akt/api-avengers-mock.git
cd api-avengers-mock

# Build and start everything
./scripts/build/quick-start.sh
```

This script will:
1. Stop existing containers
2. Clean up ports
3. Build all Docker images with `hf-` prefix
4. Start all services
5. Wait for health checks
6. Display access URLs

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **API Gateway** | http://localhost:8080 | - |
| **Eureka Dashboard** | http://localhost:8761 | - |
| **Grafana** | http://localhost:3000 | admin/admin |
| **Prometheus** | http://localhost:9090 | - |
| **Zipkin** | http://localhost:9411 | - |
| **Kibana** | http://localhost:5601 | - |
| **RabbitMQ** | http://localhost:15672 | guest/guest |

---
docker-compose ps
```

### Option 3: Build Individual Services

```bash
# Build specific service
cd user-service
mvn clean package -DskipTests

# Build with tests
mvn clean package

# Return to root
cd ..
```

### Verify Installation

```bash
# Check Eureka Dashboard (all services should be registered)
open http://localhost:8761

# Check API Gateway health
curl http://localhost:8080/actuator/health

# View logs
docker-compose logs -f
```

### Stopping Services

```bash
# Stop all services
./scripts/build/stop-all.sh

# Or manually
docker-compose down

# Stop and remove volumes (reset databases)
docker-compose down -v
```

---

## Service Endpoints

### Service Ports Table

| Service | Port | Base Path | Actuator |
|---------|------|-----------|----------|
| **API Gateway** | 8080 | /api | /actuator |
| **Eureka Server** | 8761 | / | /actuator |
| **Config Server** | 8888 | / | /actuator |
| **Campaign Service** | 8082 | /api/campaigns | /actuator |
| **Donation Service** | 8085 | /api/donations | /actuator |
| **Payment Service** | 8086 | /api/payments | /actuator |
| **Analytics Service** | 8087 | /api/analytics | /actuator |
| **Auth Service** | 8089 | /api/auth | /actuator |
| **Notification Service** | 8088 | /api/notifications | /actuator |

### Direct Service Access

Services can be accessed directly (bypassing gateway) for debugging:

```bash
# Campaign Service
curl http://localhost:8082/actuator/health

# Donation Service
curl http://localhost:8085/actuator/health
```

### Gateway Routes

All API calls should go through the API Gateway (port 8080):

```
http://localhost:8080/api/campaigns/*      → Campaign Service
http://localhost:8080/api/donations/*      → Donation Service
http://localhost:8080/api/payments/*       → Payment Service
http://localhost:8080/api/analytics/*      → Analytics Service
http://localhost:8080/api/auth/*           → Auth Service
http://localhost:8080/api/notifications/*  → Notification Service
```

---

## API Examples

### Complete Donation Flow

Run the automated test script:
```bash
./scripts/test/test-api.sh
```

Or follow these manual steps:

### 1. Create a Campaign

```bash
curl -X POST http://localhost:8080/api/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Help Build School",
    "description": "Building school in rural area",
    "targetAmount": 50000,
    "category": "EDUCATION",
    "createdBy": "Admin"
  }'
```

**Response:**
```json
{
  "id": 1,
  "title": "Help Build School",
  "description": "Building school in rural area",
  "targetAmount": 50000,
  "currentAmount": 0,
  "status": "ACTIVE",
  "category": "EDUCATION",
  "createdAt": "2025-11-21T10:30:00"
}
```

### 2. Make a Guest Donation (No Registration Required)

```bash
curl -X POST http://localhost:8080/api/donations \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": 1,
    "amount": 100.00,
    "donorEmail": "guest@example.com",
    "userId": null
  }'
```

**Response:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "campaignId": 1,
  "amount": 100.00,
  "donorEmail": "guest@example.com",
  "userId": null,
  "status": "CREATED",
  "transactionId": null,
  "createdAt": "2025-11-21T10:31:00"
}
```

### 3. Register User (Optional - for linking guest donations)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "guest@example.com",
    "password": "password123",
    "name": "John Doe",
    "role": "DONOR"
  }'
```

**Note:** The system automatically links previous guest donations with matching email.

### 4. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "guest@example.com",
    "password": "password123"
  }'
```

Save the token:
```bash
export TOKEN="<token-from-response>"
```

### 5. Make Authenticated Donation

```bash
curl -X POST http://localhost:8080/api/donations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "campaignId": 1,
    "amount": 250.00,
    "donorEmail": "guest@example.com",
    "userId": 1
  }'
```

### 6. Get Campaign Analytics (CQRS Read Model)

```bash
curl http://localhost:8080/api/analytics/campaigns/1
  -H "Authorization: Bearer $TOKEN"
```

### 4. Browse Products

```bash
# Get all products
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"

# Get specific product
curl http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN"

# Search products
curl "http://localhost:8080/api/products/search?query=Laptop" \
  -H "Authorization: Bearer $TOKEN"

# Get by category
curl http://localhost:8080/api/products/category/Electronics \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Add Items to Cart

```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "productName": "MacBook Pro",
    "price": 2499.99,
    "quantity": 2
  }'
```

### 6. View Cart

```bash
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer $TOKEN"
```

### 7. Update Cart Item Quantity

```bash
curl -X PUT "http://localhost:8080/api/cart/update?productId=1&quantity=3" \
  -H "Authorization: Bearer $TOKEN"
```

### 8. Remove from Cart

```bash
curl -X DELETE http://localhost:8080/api/cart/remove/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 9. Place Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Note**: This triggers the full saga pattern:
1. Creates order
2. Reserves inventory
3. Processes payment
4. Confirms order or rollback if payment fails

### 10. View Orders

```bash
# Get all orders
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"

# Get specific order
curl http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 11. Cancel Order

```bash
curl -X POST http://localhost:8080/api/orders/1/cancel \
  -H "Authorization: Bearer $TOKEN"
```

---

## Swagger/OpenAPI

Every service has interactive API documentation via Swagger UI.

### Access Swagger UI

| Service | Swagger URL |
|---------|-------------|
| User Service | http://localhost:8081/swagger-ui.html |
| Product Service | http://localhost:8082/swagger-ui.html |
| Inventory Service | http://localhost:8083/swagger-ui.html |
| Cart Service | http://localhost:8084/swagger-ui.html |
| Order Service | http://localhost:8085/swagger-ui.html |
| Payment Service | http://localhost:8086/swagger-ui.html |
| API Gateway | http://localhost:8080/swagger-ui.html |

### Using Swagger UI

1. **Open Swagger UI** for any service
2. **Authorize**: Click "Authorize" button, enter JWT token: `Bearer <token>`
3. **Try API**: Expand endpoints, click "Try it out", fill parameters, execute
4. **View Response**: See response code, body, and headers

### OpenAPI Specification

Download OpenAPI JSON spec:
```bash
curl http://localhost:8081/v3/api-docs > user-service-api.json
curl http://localhost:8082/v3/api-docs > product-service-api.json
```

---

## Testing

### Unit Tests (Mockito)

All services have comprehensive Mockito unit tests covering:
- Success scenarios
- Failure scenarios
- Edge cases
- Circuit breaker scenarios (Order Service)

Run tests:
```bash
# Test all services
./scripts/test/test-all.sh

# Test specific service
cd user-service && mvn test
cd product-service && mvn test
cd order-service && mvn test
cd cart-service && mvn test
```

Test coverage includes:
- **UserService**: Registration, login, password validation, user lookup
- **ProductService**: CRUD operations, search, category filtering
- **OrderService**: Order creation, saga pattern, circuit breaker, cancellation
- **CartService**: Add/remove items, quantity updates, cart expiration

### Load Testing (K6) 🆕

> **New Feature**: Comprehensive K6 load testing with realistic user scenarios

K6 load tests simulate realistic user traffic with gradual ramp-up.

#### Install K6

```bash
# macOS
brew install k6

# Linux/Windows - see: https://k6.io/docs/get-started/installation/
```

#### Run Load Tests

```bash
# Comprehensive load test (recommended)
./scripts/test/run-load-test.sh

# Test scenarios:
# - 30% User login
# - 40% Product browsing
# - 30% Complete purchase flow
# Ramps from 10 → 50 → 100 users over 7 minutes
```

#### Monitor During Load Test

1. Start Grafana: http://localhost:3000 (admin/admin)
2. Open "Microservices Overview" dashboard
3. Run load test in another terminal
4. Watch metrics in real-time:
   - Request rates increasing
   - Response times (target: P95 < 500ms)
   - Success rate (target: > 99%)
   - CPU & memory usage
   - GC activity

#### Expected Performance

```
✅ P95 response time < 500ms
✅ P99 response time < 1s
✅ Success rate > 99%
✅ No service failures
✅ Stable memory (no leaks)
```

---

## Frontend Application

A simple vanilla JavaScript SPA for interacting with the platform.

### Running the Frontend

```bash
cd frontend

# Option 1: Python HTTP Server
python3 -m http.server 8000

# Option 2: Node.js serve
npx serve -p 8000

# Option 3: PHP
php -S localhost:8000
```

Access: http://localhost:8000

### Features

- **Authentication**: Login and registration
- **Product Browsing**: View all products, search, filter by category
- **Shopping Cart**: Add/remove items, update quantities
- **Order Management**: Place orders, view order history
- **Real-time Updates**: Dynamic cart counter, order status

### API Configuration

Edit `frontend/app.js` to change API base URL:

```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

---

## Database Mock Data 🆕

> **New Feature**: Automated mock data population with 50 products and realistic inventory

Pre-populated sample data for testing and demos.

### Load Mock Data

```bash
# Automated script (recommended)
./scripts/test/populate-databases.sh
```

This loads:
- **50 Products** across 5 categories (Electronics, Fashion, Home & Garden, Sports, Books)
- **50 Inventory records** with varying stock levels (high/medium/low stock scenarios)

### Sample Data Contents

**Categories** (10 products each):
- **Electronics**: Laptops, phones, keyboards, monitors, SSDs
- **Fashion**: T-shirts, jeans, shoes, watches, accessories
- **Home & Garden**: Coffee makers, air purifiers, cookware
- **Sports**: Yoga mats, dumbbells, resistance bands
- **Books**: Novels, programming books, cookbooks

**Inventory Scenarios**:
- High stock items (500+ units) - for stress testing
- Medium stock (100-300 units) - typical availability
- Low stock (< 100 units) - scarcity scenarios

### Direct Database Access

```bash
# Connect to user database
docker exec -it postgres-user psql -U postgres -d userdb

# Connect to product database
docker exec -it postgres-product psql -U postgres -d productdb

# SQL queries
SELECT * FROM users;
SELECT * FROM products WHERE category = 'Electronics';
SELECT * FROM inventory WHERE available_quantity < 50;
```

---

## Monitoring & Observability 🆕

> **New Feature**: Pre-configured Grafana dashboards with Prometheus, Loki, and Zipkin integration

Complete observability stack with automatic configuration.

### Quick Access

| Tool | URL | Credentials | Purpose |
|------|-----|-------------|---------|
| **Grafana** | http://localhost:3000 | admin/admin | Dashboards & visualization |
| **Zipkin** | http://localhost:9411 | - | Distributed tracing |
| **Prometheus** | http://localhost:9090 | - | Metrics & queries |
| **Loki** | http://localhost:3100 | - | Log aggregation |
| **RabbitMQ** | http://localhost:15672 | admin/admin | Message queue management |
| **Eureka** | http://localhost:8761 | - | Service discovery |

### Grafana Dashboards (Pre-configured) 🆕

Grafana starts with **3 datasources** and **2 dashboards** automatically configured:

#### 1. Microservices Overview Dashboard
**What it shows:**
- Services up/down status
- Request rates by service
- Response times (P95, P99)
- Success rates
- CPU & memory usage
- JVM heap metrics

**When to use:** System health monitoring, load test observation

#### 2. JVM Metrics Dashboard
**What it shows:**
- Memory usage (heap/non-heap)
- GC rate & pause duration
- Thread count
- Per-service breakdown

**When to use:** Performance tuning, memory leak detection

**Access:** Dashboards → Browse → Select dashboard

### Zipkin (Distributed Tracing) 🆕

> **Improvement**: All logs now include trace IDs for correlation

**Quick start:**
1. Open http://localhost:9411
2. Click "Run Query"
3. Click any trace to see full request flow
4. Identify slow services and bottlenecks

**Features:**
- Complete request journey across services
- Latency breakdown per span
- Dependency visualization
- Error tracking

**Trace ID in logs:**
```
INFO [user-service,a3f2e1c4d5b6,1234567890ab] - User logged in
                    ↑           ↑
                 TraceID      SpanID
```

### Loki (Log Aggregation) 🆕

> **New Feature**: Centralized logs from all Docker containers

**Access via Grafana:**
1. Go to Explore (compass icon)
2. Select "Loki" datasource
3. Use LogQL queries

**Sample queries:**
```logql
# All logs from user-service
{container="user-service"}

# Errors only
{container=~".*-service"} |= "ERROR"

# Logs for specific trace
{container="user-service"} |= "traceId=a3f2e1c"

# Last 5 minutes, filtered
{container="order-service"} | json | level="ERROR" [5m]
```

### Prometheus (Metrics)

**Useful queries:**
```promql
# Request rate per service
rate(http_server_requests_seconds_count{job=~".*-service"}[5m])

# 95th percentile response time
histogram_quantile(0.95,
  sum(rate(http_server_requests_seconds_bucket[5m])) by (le, application)
)

# Error rate
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (application)

# JVM memory usage
jvm_memory_used_bytes{area="heap"}

# Success rate by service
100 * (
  sum(rate(http_server_requests_seconds_count{status!~"5.."}[5m])) by (application)
  /
  sum(rate(http_server_requests_seconds_count[5m])) by (application)
)
```

### RabbitMQ (Event-Driven Architecture)

**Monitor message flow:**
1. Open http://localhost:15672 (admin/admin)
2. Check "Queues" tab for:
   - Order events
   - Payment events
   - Notification events
3. Verify consumers are connected
4. Monitor message rates

**Key events:**
- `order.created` → Payment Service
- `payment.success` → Order Service, Notification Service
- `payment.failed` → Order Service (triggers saga rollback)

---

## Troubleshooting

### Services Not Starting

**Issue**: Docker containers not starting

**Solutions**:
```bash
# Check logs
docker-compose logs -f <service-name>

# Restart specific service
docker-compose restart <service-name>

# Rebuild and restart
docker-compose up -d --build <service-name>

# Check resource usage
docker stats
```

### Port Conflicts

**Issue**: Port already in use

**Solutions**:
```bash
# Find process using port (macOS/Linux)
lsof -i :8080

# Find process using port (Windows)
netstat -ano | findstr :8080

# Kill process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows

# Or change ports in docker-compose.yml
```

### Database Connection Issues

**Issue**: Cannot connect to database

**Solutions**:
```bash
# Check database containers
docker-compose ps | grep postgres

# Restart databases
docker-compose restart postgres-user postgres-product postgres-inventory postgres-order

# Reset databases (WARNING: deletes data)
docker-compose down -v
docker-compose up -d
```

### Service Not Registered in Eureka

**Issue**: Service not appearing in Eureka dashboard

**Solutions**:
1. Wait 30-60 seconds (registration takes time)
2. Check service logs: `docker-compose logs -f <service-name>`
3. Verify Eureka URL in service config
4. Restart service: `docker-compose restart <service-name>`

### JWT Authentication Failures

**Issue**: 401 Unauthorized errors

**Solutions**:
1. Verify token format: `Authorization: Bearer <token>`
2. Check token expiration
3. Re-login to get fresh token
4. Verify secret key matches across gateway and user service

### Circuit Breaker Activated

**Issue**: Order service circuit breaker open

**Solutions**:
1. Check payment service health: `curl http://localhost:8086/actuator/health`
2. Wait for circuit to half-open (30 seconds)
3. Check Grafana for error rates
4. Review order service logs

### High Memory Usage

**Issue**: Docker consuming too much memory

**Solutions**:
```bash
# Check memory usage
docker stats

# Restart Docker Desktop

# Allocate more memory to Docker (Docker Desktop settings)

# Reduce JVM heap in docker-compose.yml:
JAVA_OPTS: -Xmx512m -Xms256m
```

### Reset Everything

**Nuclear option** - start completely fresh:
```bash
# Stop all services
docker-compose down -v

# Remove all Docker resources
docker system prune -a -f

# Remove local Maven builds
rm -rf */target

# Rebuild everything
./scripts/build/build-all.sh
docker-compose up -d --build
```

### Debugging Tips

1. **Check Eureka**: http://localhost:8761 - all services should be registered
2. **Check Logs**: `docker-compose logs -f` - watch for errors
3. **Check Health**: `/actuator/health` endpoints - verify service status
4. **Check Zipkin**: http://localhost:9411 - trace request failures
5. **Check RabbitMQ**: http://localhost:15672 - verify message flow

---

## Additional Resources

- **Main README**: [README.md](README.md) - Overview and quick start
- **Database Init**: [database/init/README.md](database/init/README.md) - Mock data details
- **Scripts**: [scripts/](scripts/) - Automation scripts
- **K6 Tests**: [k6/](k6/) - Load testing scripts
- **Frontend**: [frontend/](frontend/) - Web application

---

**For more help, check service logs or open an issue on GitHub.**
