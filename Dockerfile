# Multi-stage Dockerfile for Spring Boot Application (Java 21)

# Stage 1: Build the JAR package
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy POM and download dependencies for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy application source code and compile package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create data and uploads directories
RUN mkdir -p /app/uploads /app/data

# Copy built application jar from builder stage
COPY --from=builder /workspace/target/*.jar /app/app.jar

# Copy existing product uploads into image
COPY uploads/ /app/uploads/

# Default environment configuration
ENV PORT=8080
ENV UPLOAD_DIR=/app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
