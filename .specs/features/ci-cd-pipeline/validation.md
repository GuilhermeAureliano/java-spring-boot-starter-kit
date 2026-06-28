# CI/CD Pipeline — Validation Report (Iteration 2)

**Verdict: PASS**

- **Commit range covered:** `a8a46ce..31ee368`
- **Base:** `a8a46ce` · **Head:** `31ee368` (branch `add-ci-cd`, verified clean at end)
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
| P1-F2 then runs `flywayMigrate` against same DB | `flyway-validate.yml:64` `flywayMigrate`; env `:42-45` | Task present; `dependsOn("classes")` in `build.gradle.kts:138-139` ensures migrations on classpath | ✓ |
| P1-F3 V1 exists and SHALL create table `app_meta` without errors | `src/main/resources/db/migration/V1__init.sql:4` creates `app_meta`. Flyway CI now asserts `to_regclass('public.app_meta') IS NOT NULL` after migrate (`flyway-validate.yml:66-73`). Rename mutant (`app_meta_typo`) caught by assertion. | Table existence enforced in CI | ✓ |
| P1-F4 `flywayValidate` detects checksum/name divergence → non-zero | `flyway` plugin `flywayValidate` exists; `build.gradle.kts:35` `ignoreMigrationPatterns=["*:pending"]`. Capability exists; CI design does not deterministically exercise prior-checksum drift on ephemeral DB. | ◐ (GAP-5, accepted) | ◐ |

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
| P3-4 `checkstyleMain checkstyleTest checkstyleIntegrationTest` pass on existing code | `./gradlew clean build` runs all three: `checkstyleIntegrationTest` now included in CI integration job (`ci.yml:80`) | All three pass | ✓ |
| P3-5 `./gradlew build` includes `test` + `integrationTest` + checkstyle (via `check` aggregating `integrationTest`) | `build.gradle.kts:127-128` `check { dependsOn("integrationTest") }`; `./gradlew clean build` green | `build` aggregates all gates | ✓ |

### Edge cases
- Trivy CRITICAL/HIGH → exit 1: `release-image.yml:108` (M3 mutant confirmed structural enforcement). ✓
- Tag not `v*.*.*` → no release trigger: `release-image.yml:3-6`. ✓
- Two pushes same branch → obsolete runner cancelled: `ci.yml:12-14`. ✓
- `GITHUB_TOKEN` lacking `packages: write` → push fails: `release-image.yml:51-56` uses `secrets.GITHUB_TOKEN` with `packages: write`. ✓
- Runner without Docker → `integrationTest` fails (Testcontainers): documented requirement; locally `./gradlew test` passes without Docker, `integrationTest` requires Docker. ✓

### tasks.md "Done when" checks (T1–T8)
- T1 Checkstyle ✓ · T2 Flyway plugin + V1 + `build` ✓ · T3 split (file locations + `test` no Docker + `integrationTest` + `check` aggregates) ✓ · T4 ci.yml ✓ · T5 flyway-validate.yml ✓ · T6 dependency-review + dependabot ✓ · T7 codeql.yml ✓ · T8 release-image.yml ✓.

## 2. Independent Gate Re-run Results

| Command (run by verifier, not author) | Result | Counts |
|---|---|---|
| `./gradlew --no-daemon clean test` | **PASS** (no Docker) | 7 unit cases / 4 classes |
| `./gradlew --no-daemon clean build` | **PASS** | 14 tasks executed: test + integrationTest + checkstyle* + check + build |
| `rm -rf build && FLYWAY_URL=… ./gradlew clean flywayValidate flywayMigrate` (fresh postgres) | **PASS** | `flywayValidate` + `flywayMigrate` green; `docker exec … to_regclass('public.app_meta')` → `app_meta` |
| `python3 -c "import yaml,glob; …"` over all `.github/workflows/*.yml` | **PASS** | all YAML files valid |

## 3. Discrimination Sensor (fault injection)

Mutant applied in scratch, then restored; `git status` clean at HEAD `31ee368`.

| Mutant | Gate | Result | How detected |
|---|---|---|---|
| **M2** rename table `app_meta`→`app_meta_typo` (fresh build, `flywayMigrate`) | Flyway migrate + CI assertion | **KILLED** | `to_regclass('public.app_meta')` returns empty → `[ "${result}" = "t" ]` fails; workflow step would exit 1 with `::error::flywayMigrate reported success but app_meta table is missing` |

Iteration 1 survivors (M1' empty-classpath, M2 rename) are now both killed by fixes in `52b9943` and `5d1ddfd`.

## 4. Ranked Gap List

1. **GAP-1 (CRITICAL → FIXED in `52b9943`)** — Flyway tasks now depend on `classes` (which chains `processResources`), ensuring `db/migration/V1__init.sql` is on classpath before Flyway reads it. Fresh-build regression test: `rm -rf build && ./gradlew clean flywayValidate flywayMigrate` → BUILD SUCCESSFUL, `app_meta` created. ✓

2. **GAP-2 (MEDIUM → FIXED in `5d1ddfd`)** — Workflow now asserts `to_regclass('public.app_meta') IS NOT NULL` after `flywayMigrate`. Rename mutant caught: assertion returns empty, step exits 1. ✓

3. **GAP-3 (LOW → FIXED in `31ee368`)** — CI integration job now runs `./gradlew checkstyleIntegrationTest integrationTest`. Checkstyle violations in the integrationTest source set now fail CI on PRs. ✓

4. **GAP-4 (informational, non-blocking)** — Several actions are pinned to majors newer than the literal spec text (`upload-artifact` v4→v7, `dependency-review-action` v4→v5). Accepted under AD-010 (major-tag pinning, Dependabot-maintained). Not a defect.

5. **GAP-5 (LOW, inherent)** — P1-F4 (checksum/name divergence detection) is only weakly exercisable by CI: validate→migrate→validate on a fresh ephemeral DB has no prior applied state to drift against. Structural capability exists; CI flow does not deterministically exercise it. Accepted as inherent to ephemeral-DB design.

## 5. Final Verdict

**PASS.** All three iteration-1 gaps are fixed and verified:

- **GAP-1** (flyway classpath): `52b9943` adds `dependsOn("classes")` to `flywayValidate` and `flywayMigrate`. Fresh-build regression test passed.
- **GAP-2** (app_meta assertion): `5d1ddfd` adds a `docker run psql` step asserting `to_regclass('public.app_meta') IS NOT NULL`. Rename mutant killed.
- **GAP-3** (checkstyleIntegrationTest in CI): `31ee368` adds `checkstyleIntegrationTest` to the integration job.

All Gradle gates green (unit: 7 cases, build: 14 tasks including integration). YAML valid. No remaining blocking gaps.

## Environment / Notes
- Java 21 (Corretto 21.0.2), Docker 28.3.3 / Compose v2.39.1, pyyaml 6.0.3.
- `./gradlew` wrapper used (system `gradle` not on PATH).
- All scratch mutants discarded; ephemeral postgres torn down (`docker compose down -v`).
- Final tree: clean, HEAD `31ee368` (`git status` clean confirmed).
