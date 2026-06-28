# CI/CD Pipeline (GitHub Actions) Specification

## Problem Statement

O projeto não possui pipeline de CI/CD automatizado. Build, testes, validação de migrations e
publicação de imagem são executados apenas localmente. Precisamos de um pipeline GitHub Actions
que valide PRs deterministamente, valide migrations Flyway, analise segurança da cadeia de
dependências e código, e publique uma imagem OCI atestada em tag de release. O relatório
`deep-research-report.md` fornece as melhores práticas; este spec adapta essas práticas ao estado
real do projeto (sem plugin Flyway/Checkstyle, sem migrations, testes unitários e de integração
misturados, sem cloud provider para deploy).

## Goals

- [ ] PRs falhem rápido: build, testes unitários e checkstyle em job sem Docker
- [ ] Testes de integração (Testcontainers) rodem isolados em job próprio
- [ ] Migrations Flyway sejam validadas e aplicadas contra PostgreSQL efêmero em job dedicado
- [ ] Dependent review + Dependabot + CodeQL cubram a cadeia de supply chain
- [ ] Em tag `v*`, imagem OCI seja construída, publicada no GHCR com provenance/SBOM e atestada
- [ ] Imagem publicada seja escaneada por Trivy antes de qualquer uso

## Out of Scope

| Feature | Reason |
| --- | --- |
| Deploy para Kubernetes/ECS/Cloud Run/VM | Sem cloud provider configurado; será trabalho futuro |
| Multi-platform images | Runner único ubuntu-latest; sem requisito declarado |
| OIDC para cloud | Sem cloud provider alvo |
| Pin de actions por SHA completo | Decisão do usuário: major version tag (AD-010) |
| Self-hosted runners | Sem necessidade para um starter kit |
| SBOM customizado fora do buildx | provenance/SBOM do docker/build-push-action é suficiente |
| Testes com provedores reais de IA | Mantemos API key dummy; não custamos chamadas LLM em CI |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Cloud provider/registry | GHCR (`ghcr.io`) via `GITHUB_TOKEN` | Nativo do GitHub, sem credencial extra | y |
| Deploy | Não incluído | Sem target definido pelo usuário | y |
| Split de testes | `integrationTest` source set/task (Gradle); Testcontainers separado do `test` | Report recomenda; PR feedback rápido | y |
| Plugins no build | Flyway Gradle plugin 12.9.0 + Checkstyle habilitados | Habilita jobs `flywayValidate`/`flywayMigrate` e `checkstyle*` | y |
| Migration inicial | `V1__init.sql` placeholder criando tabela `app_meta` | Prova que o pipeline Flyway funciona; não acopla a entidade JPA | y |
| Pin de actions | Major version tag (`@v6`) | Legibilidade + Dependabot mantém atualizado | y |
| Trivy severity gate | Falha em CRITICAL,HIGH | Equilíbrio sinal x ruído para um starter | y |
| Trigger de release | `push` de tags `v*.*.*` | Convenção SemVer | y |

**Open questions:** none.

---

## User Stories

### P1: CI de PR (build, unit, checkstyle, integração) ⭐ MVP

**User Story**: Como mantenedor, quero que cada PR execute build, testes unitários, checkstyle e
testes de integração automaticamente, para que regressões sejam detectadas antes do merge.

**Why P1**: É a base de confiança do pipeline; sem isso, nada mais faz sentido.

**Acceptance Criteria**:

1. WHEN um push ou pull_request ocorre em `main`/`develop` THEN o workflow `ci` SHALL executar
   `./gradlew clean test checkstyleMain checkstyleTest` (job sem dependência de Docker).
2. WHEN o job de integração roda THEN ele SHALL executar `./gradlew integrationTest` usando
   Testcontainers diretamente no runner ubuntu (sem `services:`).
3. WHEN qualquer job falha THEN relatórios (build/reports, build/test-results) SHALL ser
   publicados como artifact via `actions/upload-artifact@v4` (retenção 14 dias).
4. WHEN uma nova execução do workflow é disparada para a mesma ref THEN a execução anterior SHALL
   ser cancelada (`concurrency` com `cancel-in-progress: true`).
5. WHEN o job de testes unitários executa THEN ele SHALLrodar apenas os testes do source set
   `test` (nenhum teste Testcontainers no `./gradlew test`).

**Independent Test**: Abrir um PR e observar que os jobs `build-unit` e `integration` rodam,
sendo que `test` não requer Docker.

---

### P1: Validação de migrations Flyway em CI ⭐ MVP

**User Story**: Como mantenedor, quero que migrations Flyway sejam validadas e aplicadas contra
um PostgreSQL efêmero em cada PR, para que migrations inválidas quebrem o pipeline cedo.

**Why P1**: Migrations quebradas são causa comum de deploy failure; detectar cedo é barato.

**Acceptance Criteria**:

1. WHEN um pull_request ocorre THEN o workflow `flyway-validate` SHALL subir um serviço
   PostgreSQL (`postgres:16`) via `services:` e executar `./gradlew flywayValidate`.
2. WHEN `flywayValidate` passa THEN o job SHALL executar `./gradlew flywayMigrate` contra o
   mesmo banco efêmero para provar que a sequência é aplicável.
3. WHEN não há alteração em `db/migration` e a migration `V1__init.sql` existe THEN o job SHALL
   aplicar a migration criando a tabela `app_meta` sem erros.
4. WHEN `flywayValidate` detecta divergência de checksum/nome THEN o job SHALL falhar com código
   não-zero.

**Independent Test**: Rodar o workflow `flyway-validate` e verificar `app_meta` criada; quebrar o
checksum de V1 e observar falha.

---

### P1: Supply chain security (dependency review + Dependabot + CodeQL) ⭐ MVP

**User Story**: Como mantenedor, quero que dependências vulneráveis e falhas de segurança de
código sejam detectadas automaticamente, para que o repositório não acumule risco.

**Why P1**: Supply chain é a superfície de ataque mais explorada em pipelines.

**Acceptance Criteria**:

1. WHEN um pull_request é aberto THEN o workflow `dependency-review` SHALL executar
   `actions/dependency-review-action@v4` e falhar se uma dependência vulnerável for introduzida.
2. WHEN o workflow `codeql` dispara (push/PR/schedule) THEN ele SHALL executar análise `java-kotlin`
   e `actions` e publicar o resultado como SARIF via `actions/upload-artifact`.
3. WHEN `.github/dependabot.yml` existe THEN ele SHALL configurar atualizações semanais para os
   ecossistemas `gradle` e `github-actions`.

**Independent Test**: Adicionar uma dependência com CVE conhecida em um PR e ver
`dependency-review` falhar; verificar `dependabot.yml` válido.

---

### P2: Release de imagem OCI atestada

**User Story**: Como mantenedor, quero que tag de release (`v*`) construa e publique uma imagem
OCI no GHCR com provenance/SBOM e atestação, para que deploys consumam uma imagem rastreável.

**Why P2**: Necessário para releases, mas depende do CI P1 funcionar.

**Acceptance Criteria**:

1. WHEN uma tag `v*.*.*` é empurrada THEN o workflow `release-image` SHALL construir a imagem
   via `docker/build-push-action` e publicá-la em `ghcr.io/<repo>` com `provenance: mode=max` e
   `sbom: true`.
2. WHEN a imagem é publicada THEN o workflow SHALL gerar uma artifact attestation via
   `actions/attest@v4` com `subject-digest` igual ao digest da imagem publicada.
3. WHEN a imagem é publicada THEN um job `image-scan` SHALL executar Trivy
   (`aquasecurity/trivy-action`) no digest da imagem e falhar para severidade `CRITICAL,HIGH`.
4. WHEN o workflow de release executa THEN o `permissions` do job SHALL ser mínimo necessário
   (`contents: read`, `packages: write`, `id-token: write`, `attestations: write`).

**Independent Test**: Empurrar uma tag fake `v0.0.1-ci` e observar a imagem publicada no GHCR com
atestateção e o job `image-scan` rodando.

---

### P3: Configuração de build (Flyway plugin, Checkstyle, split de testes)

**User Story**: Como desenvolvedor, quero `build.gradle.kts` com plugin Flyway, Checkstyle e
`integrationTest` source set, para que os workflows tenham tasks Gradle reais para chamar.

**Why P3**: É habilitador técnico dos P1/P2; agrupado como tarefa de infra.

**Acceptance Criteria**:

1. WHEN `./gradlew tasks` é executado THEN ele SHALL listar `flywayValidate`, `flywayMigrate`,
   `checkstyleMain`, `checkstyleTest`, `integrationTest`.
2. WHEN `./gradlew test` é executado THEN ele SHALL executar apenas testes unitários/slice
   (nenhum estende `AbstractIntegrationTest`).
3. WHEN `./gradlew integrationTest` é executado THEN ele SHALL executar os testes que estendem
   `AbstractIntegrationTest` (Testcontainers PostgreSQL).
4. WHEN `./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest` é executado THEN
   SHALL passar sem violações no código existente.
5. WHEN `./gradlew build` é executado THEN SHALL incluir `test` + `integrationTest` + checkstyle
   (via `check` agregando `integrationTest`).

---

## Edge Cases

- WHEN o `GITHUB_TOKEN` não tem `packages: write` THEN o push de imagem SHALL falhar explícito.
- WHEN o runner não tem Docker disponível THEN `integrationTest` SHALL falhar (Testcontainers
  exige Docker runtime) — documentar requisito.
- WHEN a tag não segue `v*.*.*` THEN o workflow de release SHALL não disparar.
- WHEN dois pushes na mesma branch ocorrem THEN o runner obsoleto SHALL ser cancelado.
- WHEN Trivy encontra CRITICAL/HIGH THEN o pipeline SHALL falhar (exit-code 1).

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| CICD-01 | P3: Build config (Flyway plugin) | Execute | Pending |
| CICD-02 | P3: Build config (Checkstyle) | Execute | Pending |
| CICD-03 | P3: Build config (integrationTest source set) | Execute | Pending |
| CICD-04 | P1: workflow ci | Execute | Pending |
| CICD-05 | P1: workflow flyway-validate | Execute | Pending |
| CICD-06 | P1: workflow dependency-review + dependabot | Execute | Pending |
| CICD-07 | P1: workflow codeql | Execute | Pending |
| CICD-08 | P2: workflow release-image (build+push+attest) | Execute | Pending |
| CICD-09 | P2: workflow release-image (Trivy scan) | Execute | Pending |

**Coverage:** 9 total, 9 mapped to tasks, 0 unmapped.

---

## Success Criteria

- [ ] `./gradlew test` roda sem Docker e passa; `./gradlew integrationTest` roda com Docker e passa.
- [ ] `./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest` passa.
- [ ] `./gradlew flywayValidate` e `flywayMigrate` existem e funcionam contra PostgreSQL.
- [ ] Workflows presentes: `ci.yml`, `flyway-validate.yml`, `dependency-review.yml`, `codeql.yml`,
  `release-image.yml` + `dependabot.yml`, todos YAML válidos.
- [ ] `permissions` mínimo por workflow/job; `concurrency` definida em CI.