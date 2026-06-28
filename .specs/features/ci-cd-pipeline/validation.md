# CI/CD Pipeline — Validation Report

**Verdict: FAIL**

- **Commit range covered:** `a8a46ce..63236c6`
- **Base:** `a8a46ce` · **Head:** `63236c6` (branch `add-ci-cd`, verified clean at end)
- **Verifier:** independent re-derivation; author assumptions not inherited.
- **Method:** read-only + scratch fault-injection (all mutants restored; tree left clean at HEAD).

## 1. Per-AC Coverage (evidence-or-zero)

Legend: ✓ covered · ✗ not covered · ◐ partial / precision gap.

### P1 — CI de PR (ci.yml)

| AC | Evidence (`file:line`) | Outcome | Covered? |
|----|------------------------|---------|----------|
| P1-1 push/PR on main/develop runs `clean test checkstyleMain checkstyleTest`, no Docker | `.github/workflows/ci.yml:3-7` triggers; `ci.yml:46` runs `./gradlew … clean test checkstyleMain checkstyleTest bootJar`; `ci.yml:24-57` job has no `services:` | Job without Docker executes the required tasks (+ `bootJar`, additive) | ✓ |
| P1-2 integration job runs `./gradlew integrationTest` via Testcontainers on runner, no `services:` | `ci.yml:60-91`; `ci.yml:80` runs `integrationTest`; no `services:` block | Isolated integration job | ✓ |
| P1-3 reports published via `actions/upload-artifact@v4`, retention 14 days | `ci.yml:48-58`, `ci.yml:82-91` use `upload-artifact@v7` with `retention-days: 14` | Spec text says `@v4`; impl uses `@v7` (current major). Major-tag pinning is the accepted AD-010 decision → not a defect; retention 14d present | ✓ (info: v4→v7, AD-010) |
| P1-4 concurrency cancel-in-progress true | `ci.yml:12-14` `cancel-in-progress: true` | Per-ref cancellation | ✓ |
| P1-5 `./gradlew test` runs only unit/slice (no Testcontainers) | `src/test/java` contains only `HttpClientConfigTest, OpenApiConfigTest, GlobalExceptionHandlerTest, HealthCheckControllerTest` (no `AbstractIntegrationTest`); all Testcontainers tests live in `src/integrationTest/java`; `build.gradle.kts:106-125` separate source set. Independently `./gradlew test` passed without Docker (7 cases). | Source-set isolation verified structurally + by run | ✓ |

### P1 — Flyway validation (flyway-validate.yml)

| AC | Evidence | Outcome | Covered? |
|----|----------|---------|----------|
| P1-F1 PR triggers workflow, `services.postgres:16` with `pg_isready` healthcheck, runs `flywayValidate` | `flyway-validate.yml:4-8` (PR, path-filtered), `:27-40` (postgres:16 + healthcheck), `:61` `flywayValidate` | Triggers + service + task present | ✓ (structure) |
| P1-F2 then runs `flywayMigrate` against same DB | `flyway-validate.yml:64` `flywayMigrate`; env `:42-45` | Task present — **but see GAP-1: vacuous on fresh runner** | ✗ (GAP-1) |
| P1-F3 V1 exists and SHALL create table `app_meta` without errors | `src/main/resources/db/migration/V1__init.sql:4` creates `app_meta`. Local run (with built resources) created it. **But CI runs on fresh build dir → `app_meta` not created (GAP-1). No automated assertion that `app_meta` exists (GAP-2).** | Outcome not enforced in CI; rename mutant survives | ✗ (GAP-1, GAP-2) |
| P1-F4 `flywayValidate` detects checksum/name divergence → non-zero | `flyway` plugin `flywayValidate` exists; `build.gradle.kts:35` `ignoreMigrationPatterns=["*:pending"]`. Validation of applied-migration drift would fail, but CI flow (validate→migrate→validate on a fresh ephemeral DB) cannot deterministically exercise prior-checksum drift. | Capability exists; CI design does not deterministically exercise it | ◐ (GAP-5) |

### P1 — Supply chain

| AC | Evidence | Outcome | Covered? |
|----|----------|---------|----------|
| P1-S1 PR triggers `dependency-review-action@v4`, fails on vulnerable dep | `dependency-review.yml:4-5`, `:26` uses `dependency-review-action@v5` with `fail-on-severity: moderate` | Spec text says `@v4`; impl `@v5` (AD-010 major-tag). Fail-on-severity present | ✓ (info: v4→v5) |
| P1-S2 CodeQL `java-kotlin` + `actions`, upload SARIF via upload-artifact | `codeql.yml:30-35` matrix (java-kotlin autobuild, actions none); `:67-73` upload SARIF; `:52-65` init/autobuild/analyze | Both languages + SARIF artifact | ✓ |
| P1-S2b triggers push/PR + schedule `40 3 * * *`; `contents: read`, `security-events: write` | `codeql.yml:3-9`, `:11-14` | Triggers + permissions match (+ `actions: read`, needed) | ✓ |
| P1-S3 `dependabot.yml` weekly for `gradle` + `github-actions` | `.github/dependabot.yml:3-21` | Both ecosystems, weekly (monday) | ✓ |

### P2 — Release image (release-image.yml)

| AC | Evidence | Outcome | Covered? |
|----|----------|---------|----------|
| P2-1 tag `v*.*.*`, build-push-action, GHCR, `provenance: mode=max`, `sbom: true`, `push: true` | `release-image.yml:3-6` tags; `:72-83` build-push-action with `provenance: mode=max`, `sbom: true`, `push: true`; `:51-56` GHCR login | All required knobs present | ✓ |
| P2-2 `actions/attest@v4` with `subject-digest` = image digest, `push-to-registry: true` | `release-image.yml:85-90`; `subject-digest: ${{ steps.push.outputs.digest }}` | Attestation wired to push digest | ✓ |
| P2-3 `image-scan` job Trivy on digest, `severity: CRITICAL,HIGH`, `exit-code: "1"`, depends on `image` | `release-image.yml:92-110`; `:96` `needs: image`; `:103-109` Trivy `exit-code: "1"`, `severity: CRITICAL,HIGH` | Scan gate present (M3 structural mutant verified) | ✓ |
| P2-4 permissions minimal: `contents: read`, `packages: write`, `id-token: write`, `attestations: write` | `release-image.yml:8-9` top-level `contents: read`; `:25-29` image job declares exactly the four | Minimal permissions match | ✓ |

### P3 — Build config

| AC | Evidence | Outcome | Covered? |
|----|----------|---------|----------|
| P3-1 `./gradlew tasks` lists `flywayValidate`, `flywayMigrate`, `checkstyleMain`, `checkstyleTest`, `integrationTest` | Independently ran `./gradlew tasks --group flyway` and `--all`: all five present (also `checkstyleIntegrationTest`) | All listed tasks exist | ✓ |
| P3-2 `./gradlew test` runs only unit/slice (none extend `AbstractIntegrationTest`) | `src/test/java` contents + `./gradlew test` green without Docker (7 cases) | Isolation holds | ✓ |
| P3-3 `./gradlew integrationTest` runs Testcontainers tests | `./gradlew integrationTest` green with Docker (8 cases) | Testcontainers path exercised | ✓ |
| P3-4 `checkstyleMain checkstyleTest checkstyleIntegrationTest` pass on existing code | Independently ran `./gradlew clean checkstyleMain checkstyleTest checkstyleIntegrationTest` → BUILD SUCCESSFUL | All three pass | ✓ |
| P3-5 `./gradlew build` includes `test` + `integrationTest` + checkstyle (via `check` aggregating `integrationTest`) | `build.gradle.kts:127-128` `check { dependsOn("integrationTest") }`; `./gradlew check --dry-run` lists `checkstyleIntegrationTest`, `checkstyleMain`, `checkstyleTest`, `integrationTest`, `check`; `./gradlew clean build` green | `build` aggregates all gates | ✓ |

### Edge cases
- Trivy CRITICAL/HIGH → exit 1: `release-image.yml:108` (M3 mutant confirmed structural enforcement). ✓
- Tag not `v*.*.*` → no release trigger: `release-image.yml:3-6`. ✓
- Two pushes same branch → obsolete runner cancelled: `ci.yml:12-14`. ✓
- `GITHUB_TOKEN` lacking `packages: write` → push fails: `release-image.yml:51-56` uses `secrets.GITHUB_TOKEN` with `packages: write`. ✓
- Runner without Docker → `integrationTest` fails (Testcontainers): documented requirement; locally `./gradlew test` passes without Docker, `integrationTest` requires Docker. ✓

### tasks.md "Done when" checks (T1–T8)
- T1 Checkstyle ✓ · T2 Flyway plugin + V1 + `build` ✓ (GAP-1 caveat) · T3 split (file locations + `test` no Docker + `integrationTest` + `check` aggregates) ✓ · T4 ci.yml ✓ · T5 flyway-validate.yml ✗ (GAP-1) · T6 dependency-review + dependabot ✓ · T7 codeql.yml ✓ · T8 release-image.yml ✓.

## 2. Independent Gate Re-run Results

| Command (run by verifier, not author) | Result | Counts |
|---|---|---|
| `./gradlew --no-daemon clean test` | **PASS** (no Docker) | 7 unit cases / 4 classes (HttpClientConfigTest 2, OpenApiConfigTest 1, GlobalExceptionHandlerTest 3, HealthCheckControllerTest 1) |
| `./gradlew --no-daemon clean integrationTest` | **PASS** (Docker) | 8 cases (SecurityConfigTest 5, IntegrationTest 2, SpringBootStarterKitApplicationTests 1) |
| `./gradlew --no-daemon clean build` | **PASS** | 14 tasks: test + integrationTest + checkstyle* + check + build |
| `./gradlew --no-daemon clean checkstyleMain checkstyleTest checkstyleIntegrationTest` | **PASS** | all three checkstyle tasks clean |
| `./gradlew tasks --group flyway` / `--all` | **PASS** | flywayValidate, flywayMigrate, checkstyleMain/Test/IntegrationTest, integrationTest all exist |
| `python3 -c "import yaml,glob; …"` over all `.github/workflows/*.yml` + `dependabot.yml` | **PASS** | all 6 YAML files valid |
| Flyway end-to-end (postgres up, `flywayValidate`→`flywayMigrate`→`flywayValidate`, with prior `build`) | **PASS** | `app_meta` created, `flyway_schema_history` V1 success=true |
| **Fresh-checkout replication of CI:** `rm -rf build; ./gradlew clean flywayMigrate` | **FAIL (silent)** | exit 0 but `app_meta_exists = 0`; only `clean`+`flywayMigrate` ran (processResources NOT executed); `flywayInfo` reports "No migrations found" → **GAP-1** |

## 3. Discrimination Sensor (fault injection)

All mutants applied in scratch, then restored; `git status` clean at HEAD `63236c6` after each.

| Mutant | Gate | Result | How detected |
|---|---|---|---|
| **M1** SQL syntax error in `V1__init.sql` (`CREATE TABLE`→`CREATE TABLEX`), after `clean processResources` | `./gradlew clean processResources flywayMigrate` | **KILLED** | Gradle `BUILD FAILED` exit 1; `flyway_schema_history` empty |
| **M1'** same mutation but **without** regenerating resources (mimics CI fresh build) | `./gradlew clean flywayMigrate` | **SURVIVED** (false pass) | exit 0, `app_meta` NOT created — Flyway read empty classpath. This reproduction **is** GAP-1. |
| **M2** rename table `app_meta`→`app_meta_typo` (with `clean processResources`) | `./gradlew flywayMigrate` | **SURVIVED** | exit 0; `app_meta_typo` created instead of `app_meta`; no gate asserts the specific `app_meta` table → **GAP-2** |
| **M3** Trivy `exit-code: "1"`→`"0"` | structural AC verification (YAML read) | **KILLED** (by structural check) | AC "fail for CRITICAL,HIGH" no longer holds; would let a vulnerable image pass |
| **M4** leak `SecurityConfigTest` into `src/test/java` (keep `AbstractIntegrationTest` in integrationTest) | `./gradlew clean test` | **KILLED** | `BUILD FAILED` — compile error `cannot find symbol: class AbstractIntegrationTest`. Source-set boundary genuinely isolates Testcontainers tests. |
| **M5** (optional) move integration test back without leaking (no compile ref) — already covered by M4 | — | — | — |

Mutant tally: **killed = 4** (M1 with resources, M3, M4, plus M1' acting as the GAP-1 detector), **survived = 2** (M1' / empty-classpath false pass → GAP-1; M2 rename → GAP-2).

## 4. Ranked Gap List

1. **GAP-1 (CRITICAL → FAIL)** — Flyway CI gate is vacuous on a fresh runner. `flywayValidate`/`flywayMigrate` do **not** depend on `processResources`, so with locations `classpath:db/migration` and no `build/` dir the classpath resolves to nothing: `flywayInfo` = "No migrations found", `flywayMigrate` applies nothing, `app_meta` is never created, and the workflow still exits green. This silently violates P1-F2 ("prove the sequence is applicable") and P1-F3 ("creating table `app_meta` without errors"). Reproduced locally: `rm -rf build && ./gradlew clean flywayMigrate` → exit 0, `app_meta_exists = 0`, only `clean`+`flywayMigrate` executed.
   - Suggested fix tasks:
     - In `build.gradle.kts`: `tasks.named("flywayValidate","flywayMigrate","flywayInfo"){ dependsOn("processResources") }` (and/or `classes`), OR
     - Change `flyway.locations` to `filesystem:src/main/resources/db/migration`, OR
     - In `flyway-validate.yml`: add a step `./gradlew processResources` (or `build -x test`) before `flywayValidate`.

2. **GAP-2 (MEDIUM)** — No assertion that the `app_meta` table specifically is created. The rename mutant (`app_meta`→`app_meta_typo`) survives `flywayMigrate` (exit 0). Spec P1-F3 names `app_meta` precisely; only a manual `psql` check enforced it.
   - Suggested fix task: add a workflow step (e.g. `docker exec … psql -c "SELECT to_regclass('public.app_meta')"` asserting non-null) or a small Flyway/Gradle check, plus assert `flyway_schema_history` has `V1` with `success=true`.

3. **GAP-3 (LOW)** — CI `integration` job runs `./gradlew integrationTest` but does **not** run `checkstyleIntegrationTest`; checkstyle violations in the `integrationTest` source set would not fail CI. Local `build` does aggregate it, but the PR workflow gap means it is not enforced on PRs.
   - Suggested fix task: add `checkstyleIntegrationTest` to the integration job in `ci.yml:80` (e.g. `./gradlew integrationTest checkstyleIntegrationTest`).

4. **GAP-4 (informational, non-blocking)** — Several actions are pinned to majors newer than the literal spec text (`upload-artifact` v4→v7, `dependency-review-action` v4→v5, `checkout`/`setup-java`/etc. to v7/v5). Accepted under AD-010 (major-tag pinning, Dependabot-maintained); recorded for traceability. Not a defect.

5. **GAP-5 (LOW)** — P1-F4 (checksum/name divergence detection) is only weakly exercisable by the CI design: validate→migrate→validate on a fresh ephemeral DB has no prior applied state to drift against. The structural capability exists (`flywayValidate` fails on drift), but the CI flow does not deterministically exercise it. Not a hard AC failure.

## 5. Final Verdict

**FAIL.** All local Gradle gates, checkstyle, the unit/integration split, supply-chain workflows, and the release-image workflow are structurally sound and independently re-verified green. However, the **Flyway validation CI workflow silently false-passes on a fresh runner** (GAP-1, reproduced): `flywayMigrate`/`flywayValidate` do not pull in `processResources`, so `app_meta` is never created while the workflow exits 0 — directly defeating two P1 acceptance criteria (P1-F2 and P1-F3). The secondary gap GAP-2 (no `app_meta`-specific assertion) further weakens the gate. These must be fixed before the feature can be considered verified.

## Environment / Notes
- Java 21 (Corretto 21.0.2), Docker 28.3.3 / Compose v2.39.1, pyyaml 6.0.3.
- `./gradlew` wrapper used (system `gradle` not on PATH).
- All scratch mutants discarded; ephemeral postgres torn down (`docker compose down -v`).
- Final tree: clean, HEAD `63236c6` (`git status` clean confirmed).