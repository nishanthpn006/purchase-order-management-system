# Stage 1: Build the application artifact
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml first for Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Production runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Run as unprivileged user
RUN addgroup -S poms && adduser -S poms -G poms
USER poms

# Copy compiled jar from builder
COPY --from=builder --chown=poms:poms /app/target/*.jar app.jar

# Environment defaults
ENV PORT=5000
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE ${PORT}

# Launch Spring Boot binding to dynamic $PORT
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT} -jar app.jar"]
