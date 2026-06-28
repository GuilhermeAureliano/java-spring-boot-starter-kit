# Tasks — CI/CD Pipeline

Source: `.specs/features/ci-cd-pipeline/spec.md`. Grace: config-only tasks use Build gate
(`./gradlew build` + YAML validation); build tasks use Full gate.

Gate levels:
- Quick = `./gradlew test` (unit only, no Docker)
- Full = `./gradlew test integrationTest` (needs Docker)
- Build = `./gradlew build` + YAML validity check (`python3 -c "import yaml,sys; ..."`)

YAML-check helper (gate for workflow tasks):
`python3 -c "import yaml,glob; [yaml.safe_load(open(f)) for f in glob.glob('.github/workflows/*.yml')]; print('yaml ok')"`

---

## Phase 1 — Build hardening

### T1: Checkstyle plugin + minimal config
- **Done when**:
  - `./gradlew tasks` lista `checkstyleMain`, `checkstyleTest`.
  - `./gradlew checkstyleMain checkstyleTest` passa no código existente.
- **Files**: `build.gradle.kts`, `config/checkstyle/checkstyle.xml`
- **Tests**: none (config)
- **Gate**: Build (`./gradlew test` + `./gradlew checkstyleMain checkstyleTest`)
- **Dependencies**: none
- **Req**: CICD-02

### T2: Flyway Gradle plugin + flyway config + V1 migration
- **Done when**:
  - `./gradlew tasks` lista `flywayValidate`, `flywayMigrate`.
  - `src/main/resources/db/migration/V1__init.sql` existe criando tabela `app_meta`.
  - `./gradlew build` passa (plugin carrega sem quebrar o build).
- **Files**: `build.gradle.kts`, `src/main/resources/db/migration/V1__init.sql`
- **Tests**: none (infra)
- **Gate**: Build (`./gradlew build`)
- **Dependencies**: T1 (mesmo arquivo build.gradle.kts; ordem para diff limpo)
- **Req**: CICD-01

### T3: integrationTest source set — move Testcontainers tests
- **Done when**:
  - `src/integrationTest/java/com/example/starterkit/` contém `AbstractIntegrationTest`,
    `IntegrationTest`, `SpringBootStarterKitApplicationTests`, `config/security/SecurityConfigTest`.
  - `src/test/java` contém apenas `HttpClientConfigTest`, `OpenApiConfigTest`,
    `HealthCheckControllerTest`, `GlobalExceptionHandlerTest`.
  - `./gradlew test` executa SEM Docker e passa (4 classes unitárias/slice).
  - `./gradlew integrationTest` executa Testcontainers e passa.
  - `./gradlew check` agrega `integrationTest`.
- **Files**: `build.gradle.kts`, move 4 arquivos de teste para `src/integrationTest/java/...`
- **Tests**: os movidos (não deletados, não enfraquecidos)
- **Gate**: Full (`./gradlew test integrationTest`)
- **Dependencies**: T2 (build.gradle.kts base)
- **Req**: CICD-03

---

## Phase 2 — CI workflows

### T4: ci.yml (push/PR: unit + checkstyle + integration + artifacts)
- **Done when**:
  - `.github/workflows/ci.yml` define `on: push|pull_request` em `[main, develop]`.
  - Job `build-unit` roda `./gradlew clean test checkstyleMain checkstyleTest` sem `services:`.
  - Job `integration` roda `./gradlew integrationTest` (depende de build-unit).
  - Ambos fazem upload de `build/reports/**` e `build/test-results/**` via upload-artifact@v4.
  - `concurrency` com `cancel-in-progress: true`.
  - `permissions: contents: read` mínimo.
- **Files**: `.github/workflows/ci.yml`
- **Tests**: none (workflow)
- **Gate**: Build (`./gradlew build` + YAML-check)
- **Dependencies**: T3 (tasks existem)
- **Req**: CICD-04

### T5: flyway-validate.yml (PR: postgres service + flywayValidate/Migrate)
- **Done when**:
  - `.github/workflows/flyway-validate.yml` define `on: pull_request`.
  - `services.postgres` (postgres:16) com healthcheck `pg_isready`.
  - Roda `./gradlew flywayValidate` e `./gradlew flywayMigrate` com env `FLYWAY_URL/USER/PASSWORD`.
- **Files**: `.github/workflows/flyway-validate.yml`
- **Tests**: none
- **Gate**: Build (`./gradlew build` + YAML-check)
- **Dependencies**: T2 (flyway tasks)
- **Req**: CICD-05

### T6: dependency-review.yml + dependabot.yml
- **Done when**:
  - `.github/workflows/dependency-review.yml` roda em PR usando
    `actions/dependency-review-action@v4`.
  - `.github/dependabot.yml` configura `gradle` e `github-actions` em schedule semanal.
- **Files**: `.github/workflows/dependency-review.yml`, `.github/dependabot.yml`
- **Tests**: none
- **Gate**: Build (`./gradlew build` + YAML-check em workflows e dependabot)
- **Dependencies**: none
- **Req**: CICD-06

### T7: codeql.yml (push/PR/schedule)
- **Done when**:
  - `.github/workflows/codeql.yml` define `on: push|pull_request` em `[main, develop]` e
    `schedule: cron '40 3 * * *'`.
  - Análise `java-kotlin` e `actions`.
  - Upload de SARIF via upload-artifact@v4.
  - `permissions: contents: read`, `security-events: write`.
- **Files**: `.github/workflows/codeql.yml`
- **Tests**: none
- **Gate**: Build (`./gradlew build` + YAML-check)
- **Dependencies**: none
- **Req**: CICD-07

---

## Phase 3 — Release

### T8: release-image.yml (build+push+attest+trivy)
- **Done when**:
  - `.github/workflows/release-image.yml` define `on: push tags v*.*.*`.
  - Job `image`: login GHCR, metadata-action, build-push-action com `provenance: mode=max`,
    `sbom: true`, `push: true`.
  - Job `image`: `actions/attest@v4` com `subject-digest` da imagem, `push-to-registry: true`.
  - Job `image-scan`: Trivy no digest, `severity: CRITICAL,HIGH`, `exit-code: "1"`, depende de image.
  - `permissions`: `contents: read`, `packages: write`, `id-token: write`, `attestations: write`.
- **Files**: `.github/workflows/release-image.yml`
- **Tests**: none
- **Gate**: Build (`./gradlew build` + YAML-check)
- **Dependencies**: T4 (CI validado)
- **Req**: CICD-08, CICD-09

---

## Execution Plan (phases = 3 → inline)

T1 → T2 → T3 (Phase 1) → T4 → T5 → T6 → T7 (Phase 2) → T8 (Phase 3) → Verifier.