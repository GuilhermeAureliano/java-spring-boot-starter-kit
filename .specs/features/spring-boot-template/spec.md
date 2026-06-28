# Spring Boot Starter Kit Specification

## Problem Statement

Starting a new Java + Spring Boot project requires repeating the same initial setup: Gradle configuration, dependency versions, database connection, observability, validation, and package structure. This template eliminates that repetition by providing a reusable, well-configured foundation with compatible versions and common dependencies already wired.

## Goals

- [ ] Provide a working Spring Boot 3.4 + Java 21 + Gradle 8 project that compiles and tests cleanly.
- [ ] Include a compatible, stable stack: Spring Boot, Spring AI, PostgreSQL driver, and Gradle.
- [ ] Exclude Lombok and rely on explicit Java patterns (records, constructors, getters/setters where needed).
- [ ] Include common Spring Boot dependencies and configurations ready for production-like services.
- [ ] Offer a sensible package structure and a minimal runnable example with tests.

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Authentication/authorization | Domain-specific; template should remain generic. |
| Front-end assets or SPA | Template is backend-only. |
| Cloud-specific deployment manifests (K8s, Docker Compose) | Out of scope for the base template. |
| Full business domain implementation | Template provides only a minimal example. |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --------------------- | -------------- | --------- | ---------- |
| Java version | 21 LTS | Stable, supported, required by Spring Boot 3.4. | y (agent) |
| Spring Boot version | 3.4.5 | Current stable patch in the 3.4 line. | y (agent) |
| Gradle version | 8.13 | Stable release compatible with Spring Boot 3.4 plugin. | y (agent) |
| Spring AI version | 1.0.0-M7 | Latest milestone aligned with Spring Boot 3.4; marked as milestone in BOM. | y (agent) |
| PostgreSQL driver | 42.7.5 | Current stable driver; compatible with Spring Boot 3.4. | y (agent) |
| Build tool | Gradle with Kotlin DSL | Idiomatic for modern Spring Boot projects. | y (agent) |
| Lombok | Excluded | Explicit code preferred; no annotation preprocessing. | y (user) |
| Package structure | `com.example.starterkit` | Common placeholder for a template. | y (agent) |
| Database migrations | Flyway | Widely used, production-ready, integrates with Spring Boot. | y (agent) |

**Open questions:** none — all resolved or logged above.

---

## User Stories

### P1: Reusable Project Skeleton ⭐ MVP

**User Story**: As a developer, I want a clone-able repository that compiles and runs with a single command so that I can start a new service without manual setup.

**Why P1**: This is the core purpose of the template.

**Acceptance Criteria**:

1. WHEN the project is cloned THEN `./gradlew build` SHALL compile the application, run tests, and produce a runnable jar.
2. WHEN the application starts THEN it SHALL expose a health endpoint (`/actuator/health`) returning HTTP 200.
3. WHEN a developer inspects the build file THEN it SHALL declare stable, compatible versions for Java, Spring Boot, Spring AI, Gradle, and PostgreSQL without Lombok.

**Independent Test**: Clone, run `./gradlew build`, start with `./gradlew bootRun`, and curl `/actuator/health`.

---

### P2: Common Dependencies Pre-Configured

**User Story**: As a developer, I want common backend dependencies already declared and configured so that I don't have to research each one.

**Why P2**: Reduces the time to add the first feature.

**Acceptance Criteria**:

1. WHEN the build file is read THEN it SHALL include Spring Web, Spring Data JPA, Validation, Actuator, DevTools, PostgreSQL, Flyway, Spring AI, and testing helpers (Testcontainers).
2. WHEN the application starts with `spring.profiles.active=dev` THEN it SHALL use an in-memory H2 database automatically (no external PostgreSQL required for local development).
3. WHEN the application starts with `spring.profiles.active=prod` THEN it SHALL connect to PostgreSQL via configuration placeholders.

**Independent Test**: Run `./gradlew dependencies --configuration runtimeClasspath` and verify listed artifacts are present; start with `dev` profile and confirm H2 console/health is accessible.

---

### P3: Minimal Example Domain and Tests

**User Story**: As a developer, I want a small working example (entity, repository, service, controller, tests) so that I understand the intended structure.

**Why P3**: Helps onboarding and validates the template works end-to-end.

**Acceptance Criteria**:

1. WHEN a `GET /api/items` request is made THEN it SHALL return a list of persisted items from the database.
2. WHEN a `POST /api/items` request is made with valid JSON THEN it SHALL create and return the item.
3. WHEN the test suite runs THEN it SHALL include unit and integration tests for the example domain with the application context loading successfully.

**Independent Test**: Run `./gradlew test` and verify all tests pass; exercise the `/api/items` endpoints with curl or HTTP client.

---

## Edge Cases

- WHEN `./gradlew build` runs on a fresh clone without local Gradle installation THEN the wrapper SHALL download Gradle automatically and complete successfully.
- WHEN the application starts without `DATABASE_URL` in production profile THEN it SHALL fail fast with a clear error about missing PostgreSQL configuration.
- WHEN a `POST /api/items` request contains invalid JSON THEN it SHALL return HTTP 400 with a validation error.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| SBK-01 | P1: Reusable Project Skeleton | Build | Pending |
| SBK-02 | P1: Reusable Project Skeleton | Runtime | Pending |
| SBK-03 | P1: Reusable Project Skeleton | Build | Pending |
| SBK-04 | P2: Common Dependencies Pre-Configured | Build | Pending |
| SBK-05 | P2: Common Dependencies Pre-Configured | Runtime | Pending |
| SBK-06 | P2: Common Dependencies Pre-Configured | Runtime | Pending |
| SBK-07 | P3: Minimal Example Domain and Tests | Runtime | Pending |
| SBK-08 | P3: Minimal Example Domain and Tests | Runtime | Pending |
| SBK-09 | P3: Minimal Example Domain and Tests | Test | Pending |

**Coverage:** 9 total, 0 mapped to tasks, 9 unmapped ⚠️

---

## Success Criteria

- [ ] `./gradlew build` completes successfully on a clean clone.
- [ ] `./gradlew test` runs without failures.
- [ ] Application starts in `dev` profile without external dependencies.
- [ ] `/actuator/health` returns HTTP 200 when the application is running.
- [ ] No Lombok dependency or plugin is present in the build.
