# Repository Guidelines

## Project Structure & Module Organization

This is a Java 21 Spring Boot starter project using Gradle Kotlin DSL. Application code lives under `src/main/java/com/example/starterkit`, organized by concern: `config`, `error`, and `health`. Runtime configuration is in `src/main/resources`, with profile-specific files such as `application-dev.yml` and `application-prod.yml`. Tests mirror the main package under `src/test/java/com/example/starterkit`; test resources live in `src/test/resources`. Infrastructure examples are kept at the root (`Dockerfile`, `docker-compose.yml`) and in `k8s/`.

## Build, Test, and Development Commands

- `sdk env`: load the Java 21 and Gradle 8.13 versions from `.sdkmanrc`.
- `./gradlew bootRun`: run the application locally on `http://localhost:8080`.
- `./gradlew test`: run the JUnit Platform test suite.
- `./gradlew build`: compile, test, and package the application.
- `docker compose up --build`: start the app and PostgreSQL together.
- `docker compose up postgres -d`: start only PostgreSQL for local development.
- `docker build -t starterkit:latest .`: build the production container image.

## Coding Style & Naming Conventions

Use Java conventions with 4-space indentation and clear package boundaries. Name application classes by role, for example `HealthCheckController`, `SecurityConfig`, or `GlobalExceptionHandler`. Configuration properties should be explicit and grouped under meaningful prefixes in YAML. Prefer constructor injection for application components and keep profile-specific behavior in configuration files rather than inline conditionals.

## Testing Guidelines

Tests use JUnit 5, Spring Boot Test, Spring Security Test, and Testcontainers for PostgreSQL integration coverage. Place unit and slice tests beside matching package paths in `src/test/java`. Use `*Test` for test class names, and reserve broader integration coverage for classes like `IntegrationTest` or tests extending `AbstractIntegrationTest`. Run `./gradlew test` before opening a pull request; use `./gradlew build` when packaging or dependency changes are involved.

## Commit & Pull Request Guidelines

Recent history follows Conventional Commits such as `feat(config): ...`, `build(docker): ...`, `test(health): ...`, and `docs(readme): ...`. Keep commits scoped and imperative, with the affected area in parentheses when helpful. Pull requests should include a short summary, testing performed, linked issues or specs when applicable, and screenshots or logs for user-facing, Docker, or Kubernetes behavior changes.

## Security & Configuration Tips

Do not commit real credentials. Development defaults live in `application-dev.yml`, while production expects environment variables such as `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, and `OPENAI_API_KEY`. Review Docker and Kubernetes changes for non-root execution, resource limits, probes, and secret handling.
