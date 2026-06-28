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

# Extract Spring Boot layered JAR for optimal image layers
RUN java -Djarmode=tools -jar build/libs/*.jar extract --layers --launcher --destination extracted

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# Create non-root user
RUN useradd --no-log-init -r -u 10001 -g root appuser

# Copy extracted layers from builder (least to most mutable)
COPY --from=builder /workspace/extracted/dependencies/ ./
COPY --from=builder /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/extracted/application/ ./

# Run as non-root
USER 10001

EXPOSE 8080

# JVM tuned for containers: explicit heap limit, GC logging, container awareness
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=65", \
  "-Xlog:gc*:stdout:time,uptime,level,tags", \
  "org.springframework.boot.loader.launch.JarLauncher"]
