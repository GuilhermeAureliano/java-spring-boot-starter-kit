# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build scripts first for efficient caching
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./

# Download dependencies using BuildKit cache
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon || true

# Copy source and build the application
COPY src/ src/
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# Create non-root user
RUN useradd --no-log-init -r -u 10001 -g root appuser

# Copy the application JAR from the build stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Run as non-root
USER 10001

EXPOSE 8080

# JVM tuned for containers: explicit heap limit, GC logging, container awareness
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=65", \
  "-Xlog:gc*:stdout:time,uptime,level,tags", \
  "-jar", "app.jar"]
