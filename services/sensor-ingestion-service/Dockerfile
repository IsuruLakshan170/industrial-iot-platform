# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Build the app with Maven (Java 21)
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory inside the container
WORKDIR /app

# Copy only the Maven descriptor first to leverage Docker layer caching
COPY pom.xml .

# Pre-fetch dependencies (speeds up subsequent builds if pom.xml doesn't change)
RUN mvn -q -B -DskipTests dependency:go-offline

# Now copy the source code
COPY src ./src

# Build the Spring Boot JAR (skip tests for faster local builds)
RUN mvn -q -B -DskipTests package

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Runtime image (small JRE-only image, Java 21)
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

# Security best-practice: create a non-root user
RUN useradd -ms /bin/bash appuser

# Create app directory and copy the built artifact from the build stage
WORKDIR /opt/app
COPY --from=build /app/target/*.jar ./app.jar

# Expose HTTP port (matches server.port default 8080)
EXPOSE 8080

# Switch to non-root user
USER appuser

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]