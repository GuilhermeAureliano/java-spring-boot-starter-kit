# Spring Boot Starter Kit Tasks

**Spec**: `.specs/features/spring-boot-template/spec.md`
**Design**: `.specs/features/spring-boot-template/design.md`

---

## Gate Check Commands

| Gate level | Command |
| ---------- | ------- |
| Quick | `./gradlew test` |
| Full | `./gradlew test` (no E2E suite in this template) |
| Build | `./gradlew build` |

---

## Test Coverage Matrix

| Layer | Coverage expectation |
| ----- | -------------------- |
| Build/Config | `./gradlew build` passes; no Lombok in dependency report; Java 21 toolchain active. |
| Controller | Happy path + validation error for example endpoints. |
| Integration | Application context loads; database migrations run; repository integration test. |

---

## Execution Plan

### T1: Bootstrap Gradle and Spring Boot build

**Priority**: P1
**Dependencies**: none
**Done when**:
- `build.gradle.kts`, `settings.gradle.kts`, and Gradle wrapper files exist.
- Java 21 toolchain is configured.
- Spring Boot 3.4.5, Spring Dependency Management, and Spring AI BOM are declared.
- Required dependencies are present: Spring Web, Data JPA, Validation, Actuator, DevTools, PostgreSQL, H2, Flyway, Spring AI OpenAI starter, Spring Boot Test, Testcontainers PostgreSQL.
- Lombok is absent from build files.
- `./gradlew build` compiles successfully (no source code yet is acceptable if build is valid).

**Tests**: none (config-only task)
**Gate**: Build

---

### T2: Add profile-specific application configuration

**Priority**: P1
**Dependencies**: T1
**Done when**:
- `application.yml`, `application-dev.yml`, and `application-prod.yml` exist.
- `dev` profile uses H2 in-memory database.
- `prod` profile connects to PostgreSQL via `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` placeholders and fails fast if missing.
- Actuator health endpoint is enabled.
- `./gradlew build` still passes.

**Tests**: none (config-only task)
**Gate**: Build

---

### T3: Implement example Item domain

**Priority**: P1
**Dependencies**: T2
**Done when**:
- `Item` JPA entity exists under `com.example.starterkit.item`.
- `ItemRepository` extends `JpaRepository`.
- `ItemService` provides `listAll()` and `create(ItemRequest)`.
- `ItemController` exposes `GET /api/items` and `POST /api/items`.
- DTOs use Java records with Jakarta Validation annotations.
- No Lombok annotations are used.

**Tests**:
- `ItemControllerTest`: `GET /api/items` returns items; `POST /api/items` with valid input returns 201 and created item; `POST /api/items` with invalid input returns 400.
- `ItemServiceTest`: `listAll` and `create` behavior with mocked repository.

**Gate**: Quick

---

### T4: Add migrations and integration tests

**Priority**: P1
**Dependencies**: T3
**Done when**:
- Flyway migration `V1__create_items_table.sql` creates the `items` table.
- `ItemRepositoryIntegrationTest` verifies entity persistence with `@DataJpaTest`.
- `ApplicationContextTest` verifies the Spring context loads.
- `ItemControllerIntegrationTest` verifies endpoints end-to-end with `@SpringBootTest` and H2.
- `./gradlew test` passes.

**Tests**: integration tests above
**Gate**: Quick

---

### T5: Final build verification and documentation

**Priority**: P1
**Dependencies**: T4
**Done when**:
- `./gradlew build` produces a runnable jar.
- `./gradlew bootRun` starts the application in `dev` profile.
- `GET /actuator/health` returns `{"status":"UP"}`.
- README.md documents how to build, run, and use the template.
- `.gitignore` ignores build artifacts and IDE files.

**Tests**: none (verification + docs task)
**Gate**: Build

---

## Requirement Traceability Update

| Requirement ID | Task | Status |
| -------------- | ---- | ------ |
| SBK-01 | T1 | Pending |
| SBK-02 | T5 | Pending |
| SBK-03 | T1 | Pending |
| SBK-04 | T1 | Pending |
| SBK-05 | T2 | Pending |
| SBK-06 | T2 | Pending |
| SBK-07 | T3, T4 | Pending |
| SBK-08 | T3, T4 | Pending |
| SBK-09 | T3, T4 | Pending |
