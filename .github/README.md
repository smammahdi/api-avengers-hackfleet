# GitHub Actions CI/CD Pipeline

Automated build, test, and deployment for the E-Commerce Microservices Platform.

## 🎯 Pipeline Overview

**Smart CI/CD** with change detection - only builds what changed:

```
┌──────────────────────────────────────────────────┐
│  TRIGGER: Push/PR to main or develop             │
└─────────────────┬────────────────────────────────┘
                  │
                  ▼
┌──────────────────────────────────────────────────┐
│  1. CHANGE DETECTION (Path Filters)              │
│     → Identifies modified services               │
│     → Skips unchanged services                   │
│     → Saves 15-20 minutes                        │
└─────────────────┬────────────────────────────────┘
                  │
                  ▼
┌──────────────────────────────────────────────────┐
│  2. BUILD & TEST (Maven + JUnit 5)               │
│     ├─ mvn clean compile                         │
│     ├─ mvn test  (Mockito unit tests)            │
│     └─ mvn package  (executable JAR)             │
│                                                  │
│     ✓ Parallel execution for speed               │
│     ✓ Test reports generated                     │
└─────────────────┬────────────────────────────────┘
                  │
                  ▼ (main branch only)
┌──────────────────────────────────────────────────┐
│  3. DOCKER BUILD & PUSH                          │
│     ├─ Two-stage Dockerfile:                     │
│     │  ├─ Build: Maven (compile + package)       │
│     │  └─ Runtime: JRE 17 (minimal)              │
│     ├─ Layer caching enabled                     │
│     ├─ Version: v1.0.<build>-<sha>               │
│     └─ Push: smamm/ecommerce-<service>           │
└─────────────────┬────────────────────────────────┘
                  │
                  ▼
┌──────────────────────────────────────────────────┐
│  4. DEPLOYMENT SUMMARY                           │
│     → Published images list                      │
│     → Docker pull commands                       │
│     → Deployment instructions                    │
└──────────────────────────────────────────────────┘
```

## 🚀 Quick Setup

### 1. Get Docker Hub Access Token

1. Go to https://hub.docker.com/
2. Login with account: **smamm**
3. **Account Settings** → **Security** → **New Access Token**
4. Name: `github-actions`
5. Permissions: **Read, Write, Delete**
6. **Copy the token** (shown only once!)

### 2. Add GitHub Secrets

Repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Add these secrets:

| Name | Value |
|------|-------|
| `DOCKER_USERNAME` | `smamm` |
| `DOCKER_PASSWORD` | (your Docker Hub token) |

### 3. Done!

Push to `main` branch → Pipeline runs automatically → Images published to Docker Hub

## 📋 What Gets Built & Tested

### Build Tools

- **Java**: OpenJDK 17 (Temurin distribution)
- **Build Tool**: Maven 3.9+
- **Test Framework**: JUnit 5
- **Mocking**: Mockito
- **Docker**: Buildx with multi-platform support

### Build Stages per Service

#### 1. Compilation (`mvn clean compile`)
- Validates Java syntax
- Resolves dependencies
- Compiles source code
- Checks for compilation errors

#### 2. Testing (`mvn test`)
- Runs JUnit 5 unit tests
- Uses Mockito for mocking dependencies
- Generates test reports
- Fails build if tests fail

**Test Coverage:**
- User Service: Registration, login, JWT generation
- Product Service: CRUD, search, category filtering
- Order Service: Saga pattern, circuit breaker, rollback
- Cart Service: Add/remove items, expiry, Redis ops
- Others: Core business logic validation

#### 3. Packaging (`mvn package`)
- Creates executable JAR file
- Includes all dependencies
- Ready for Docker build

#### 4. Docker Build (main branch only)
**Two-Stage Dockerfile:**

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Benefits:**
- Small final image (JRE only, no Maven)
- Fast rebuild with layer caching
- Security: only runtime dependencies

#### 5. Docker Push
- Tags: `v1.0.<build>-<sha>` and `latest`
- Registry: Docker Hub `smamm/ecommerce-*`
- Layer caching for faster builds
- Metadata: build date, VCS ref, version

## 🎨 Image Naming & Versioning

### Naming Convention
```
smamm/ecommerce-<service-name>:<version>
```

### Services
```
smamm/ecommerce-eureka-server
smamm/ecommerce-config-server
smamm/ecommerce-api-gateway
smamm/ecommerce-user-service
smamm/ecommerce-product-service
smamm/ecommerce-inventory-service
smamm/ecommerce-cart-service
smamm/ecommerce-order-service
smamm/ecommerce-payment-service
smamm/ecommerce-notification-service
smamm/ecommerce-frontend
```

### Version Tags
Each build gets TWO tags:

1. **Semantic Version**: `v1.0.<build_number>-<commit_sha>`
   - Example: `v1.0.42-a3f2e1c`
   - Unique per build
   - Traceable to exact commit

2. **Latest**: `latest`
   - Always points to most recent build
   - Easy for development/testing

**Example:**
```bash
# Pull specific version
docker pull smamm/ecommerce-user-service:v1.0.42-a3f2e1c

# Pull latest
docker pull smamm/ecommerce-user-service:latest
```

## 🔄 How It Works

### On Pull Request
```
✓ Detects changes
✓ Builds changed services
✓ Runs tests
✓ Reports status
✗ Does NOT push images
```

**Purpose:** Validate changes before merge

### On Push to Main
```
✓ Detects changes
✓ Builds changed services
✓ Runs tests
✓ Builds Docker images
✓ Pushes to Docker Hub
✓ Creates deployment summary
```

**Purpose:** Deploy validated changes

## 📊 Monitoring Pipeline

### View Workflow Runs
1. Go to repository
2. Click **Actions** tab
3. See all runs with status: ✅ ❌

### Check Build Logs
1. Click on a workflow run
2. Click on a job (e.g., "Build & Test Services")
3. Expand service to see logs

### Deployment Summary
After successful main branch build:
- Check "Summary" tab
- See published images
- Copy `docker pull` commands

## 🚢 Using Built Images

### Update docker-compose.yml (Optional)

Replace `build` with `image`:

```yaml
# Before (local build)
user-service:
  build:
    context: ./user-service

# After (use Docker Hub image)
user-service:
  image: smamm/ecommerce-user-service:latest
```

### Deploy
```bash
# Pull latest images
docker-compose --profile full pull

# Restart services with new images
docker-compose --profile full up -d

# Check versions
docker images | grep smamm/ecommerce
```

## 🛠️ Customization

### Change Docker Registry

To use GitHub Container Registry instead:

1. Update `.github/workflows/main-cicd.yml`:
```yaml
env:
  DOCKER_USERNAME: ${{ github.repository_owner }}
  DOCKER_REGISTRY: ghcr.io
```

2. Update secrets:
   - `DOCKER_USERNAME`: your GitHub username
   - `DOCKER_PASSWORD`: GitHub Personal Access Token with `write:packages`

3. Images will be at: `ghcr.io/<username>/ecommerce-<service>`

### Add Deployment Step

Add to workflow after docker-push:

```yaml
- name: Deploy to Production
  run: |
    ssh user@server.com 'cd /app && docker-compose pull && docker-compose up -d'
```

(Add SSH key as secret)

### Manual Trigger

Add to `on:` section:

```yaml
on:
  workflow_dispatch:  # Enables manual trigger
  push:
    branches: [main, develop]
```

Then: Actions tab → Select workflow → Run workflow

## 🐛 Troubleshooting

### "docker login failed"
- Check `DOCKER_USERNAME` and `DOCKER_PASSWORD` secrets
- Verify token hasn't expired
- Ensure token has correct permissions

### "Build failed for service X"
- Check workflow logs for specific error
- Common causes:
  - Maven compilation error
  - Test failures
  - Missing dependencies
- Run locally: `cd <service> && mvn clean test`

### "No images pushed"
- Expected on PR (images only pushed on main)
- Check if on correct branch
- Verify workflow completed successfully

### "Tests failing in CI but not locally"
- Check Java version (must be 17)
- Verify dependencies: `mvn clean install`
- Check for environment-specific issues

## 📈 Best Practices

1. **Test locally first**: `mvn clean test` before pushing
2. **Use feature branches**: Create PR for review
3. **Keep builds green**: Fix failing tests immediately
4. **Monitor build times**: Optimize slow services
5. **Review deployment summary**: Verify correct versions
6. **Tag releases**: For production deployments
7. **Rotate tokens**: Refresh Docker Hub token periodically

## 📚 Additional Resources

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Docker Hub](https://hub.docker.com/u/smamm)
- [Maven Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

---

**Questions?** Check workflow logs or open an issue.
