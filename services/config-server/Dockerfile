# Multi-stage build for Config Server

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app


COPY --from=build /app/target/*.jar app.jar

EXPOSE 8888

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8888/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
