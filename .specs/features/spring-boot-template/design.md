# Spring Boot Starter Kit Design

**Spec**: `.specs/features/spring-boot-template/spec.md`
**Status**: Draft

---

## Architecture Overview

The project is a standard Spring Boot 3.4 layered monolith intended as a clone-able template. It uses Gradle with Kotlin DSL, Java 21, and a stable set of dependencies. The application is profile-aware:

- `dev` profile: embedded H2 database for zero-config local development.
- `prod` profile: PostgreSQL connection via environment-driven configuration.

A minimal example domain (`Item`) demonstrates the intended package layout and wiring across entity, repository, service, controller, and integration tests.

```mermaid
graph TD
    A[HTTP Client] -->|GET/POST /api/items| B[ItemController]
    B --> C[ItemService]
    C --> D[ItemRepository]
    D --> E[(H2 dev / PostgreSQL prod)]
    F[Actuator] --> G[/actuator/health]
```

---

## Code Reuse Analysis

### Existing Components to Leverage

No existing codebase; this is a green-field template. Reuse is limited to Spring Boot starters and conventions.

### Integration Points

| System | Integration Method |
| ------ | ------------------ |
| H2 (dev) | `spring-boot-starter-data-jpa` + H2 runtime dependency + `application-dev.yml` |
| PostgreSQL (prod) | PostgreSQL driver + `application-prod.yml` placeholders |
| Flyway | `flyway-core` + migrations under `src/main/resources/db/migration` |
| Spring AI | `spring-ai-starter-model-openai` (or autoconfigure starter) aligned with Spring AI BOM |

---

## Components

### Build Configuration

- **Purpose**: Define the Java toolchain, dependency versions, and Gradle wrapper.
- **Location**: `build.gradle.kts`, `settings.gradle.kts`, `gradle/wrapper/gradle-wrapper.properties`
- **Interfaces**: `./gradlew build`, `./gradlew test`, `./gradlew bootRun`
- **Dependencies**: Gradle 8.13, Spring Boot Gradle plugin 3.4.5, Spring Dependency Management plugin, Java 21 toolchain.
- **Reuses**: Spring Boot BOM via plugin; Spring AI BOM via dependency management.

### Application Configuration

- **Purpose**: Provide profile-specific runtime configuration.
- **Location**: `src/main/resources/application.yml`, `application-dev.yml`, `application-prod.yml`
- **Interfaces**: Spring Boot property resolution per active profile.
- **Dependencies**: Spring Boot configuration processor (optional), H2, PostgreSQL, Flyway.
- **Reuses**: Spring Boot externalized configuration conventions.

### Example Domain: Item

- **Purpose**: Demonstrate end-to-end wiring and serve as a verification target.
- **Location**: `com.example.starterkit.item`
- **Interfaces**:
  - `ItemController.getItems(): List<ItemResponse>`
  - `ItemController.createItem(ItemRequest): ItemResponse`
  - `ItemService.listAll(): List<Item>`
  - `ItemService.create(ItemRequest): Item`
- **Dependencies**: Spring Web, Spring Data JPA, Jakarta Validation.
- **Reuses**: Standard Spring layered architecture.

### Tests

- **Purpose**: Verify the template builds, context loads, and the example domain behaves correctly.
- **Location**: `src/test/java/...` and `src/test/resources/`
- **Interfaces**: JUnit 5, Spring Boot Test, Testcontainers (for optional PostgreSQL integration test).
- **Dependencies**: `spring-boot-starter-test`, Testcontainers PostgreSQL module.
- **Reuses**: Spring Boot test slices and `@SpringBootTest`.

---

## Data Models

### Item (JPA Entity)

```java
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    // getters, setters, constructors
}
```

**Relationships**: Standalone example entity; no relationships.

### ItemRequest (DTO)

```java
public record ItemRequest(
    @NotBlank String name,
    @Size(max = 500) String description
) {}
```

### ItemResponse (DTO)

```java
public record ItemResponse(Long id, String name, String description) {}
```

---

## Error Handling Strategy

| Error Scenario | Handling | User Impact |
| -------------- | -------- | ----------- |
| Invalid request payload | Spring `@Valid` returns 400 with `MethodArgumentNotValidException` details. | HTTP 400 with field errors. |
| Missing prod database config | Spring Boot fails fast on missing required properties. | Clear startup failure message. |
| Database unavailable | Default Spring Data exception translation. | HTTP 500 on runtime failure. |

---

## Risks & Concerns

| Concern | Location | Impact | Mitigation |
| ------- | -------- | ------ | ---------- |
| Spring AI 1.0.0-M7 is a milestone, not GA. | `build.gradle.kts` | API instability or breaking changes before GA. | Use BOM for version alignment; document milestone status in README; make AI starter optional if it complicates builds. |
| No Lombok increases boilerplate. | Domain classes | More verbose entity/DTO code. | Use Java records for DTOs; keep entities small. |
| Template may become outdated. | Build files | New Spring Boot/Gradle releases. | Pin versions explicitly and document update path. |

> Additional concerns will be added if discovered during implementation.

---

## Tech Decisions

| Decision | Choice | Rationale |
| -------- | ------ | --------- |
| Java toolchain | 21 | LTS, required for Spring Boot 3.4, modern language features. |
| Gradle DSL | Kotlin DSL | Type-safe, IDE-friendly, idiomatic for Spring Boot 3.x. |
| Profile-based database | dev=H2, prod=PostgreSQL | Zero-config local run; explicit prod configuration. |
| Lombok exclusion | None | User requirement; explicit code preferred. |
| Migration tool | Flyway | Stable, simple, production-proven. |
| AI integration | Spring AI OpenAI starter | Most common Spring AI use case; aligned with BOM. |
| DTOs | Java records | Concise, immutable, no Lombok needed. |

**Project-level decisions:** none yet; this is the first feature.
