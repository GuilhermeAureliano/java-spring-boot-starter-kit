# Template Infrastructure Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user — do not proceed without it.**

---

**Design**: `.specs/features/template-infra/design.md`
**Status**: Done

---

## Test Coverage Matrix

> Generated from codebase, project guidelines, and spec — confirm before Execute. Guidelines found: none — strong defaults applied.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| ---------- | ------------------ | -------------------- | ---------------- | ----------- |
| Config / Bean | unit | Bean wiring, property binding, conditional logic | `src/test/java/**/config/*Test.java` | `./gradlew test` |
| Controller | unit (MockMvc) | Happy path + error paths for each endpoint | `src/test/java/**/controller/*Test.java` or `src/test/java/**/*ControllerTest.java` | `./gradlew test` |
| Security | integration | Auth required/public paths, credentials | `src/test/java/**/security/*Test.java` | `./gradlew test` |
| Error Handler | unit (MockMvc) | Each exception type → correct status + body | `src/test/java/**/error/*Test.java` | `./gradlew test` |
| Config / YAML | none | — (build gate only) | — | build gate only |

## Parallelism Assessment

> Generated from codebase — confirm before Execute.

| Test Type | Parallel-Safe? | Isolation Model | Evidence |
| --------- | -------------- | --------------- | -------- |
| Unit (MockMvc) | Yes | Per-test Spring context, no shared state | `@WebMvcTest` isolates controllers |
| Integration (SpringBootTest + Testcontainers) | No | Shared PostgreSQL container via `@Container` static | `AbstractIntegrationTest.java` uses static shared container |

## Gate Check Commands

> Generated from codebase — confirm before Execute.

| Gate Level | When to Use | Command |
| ---------- | ----------- | ------- |
| Quick | After tasks with unit tests only | `./gradlew test` |
| Full | After tasks with integration tests | `./gradlew test` |
| Build | After phase completion or config/entity-only tasks | `./gradlew build` |

---

## Execution Plan

### Phase 1: Dependencies & Foundation (Sequential)

```
T1 → T2 → T3
```

### Phase 2: Core Features (Parallel OK)

```
     ┌→ T4 ─┐
T3 ──┼→ T5 ─┼──→ T8
     └→ T6 ─┘
     └→ T7 ─┘
```

### Phase 3: Integration & Verification (Sequential)

```
T8 → T9
```

---

## Task Breakdown

### T1: Adicionar dependências Gradle

**What**: Adicionar todas as dependências necessárias para as 6 features no build.gradle.kts
**Where**: `build.gradle.kts`
**Depends on**: None
**Reuses**: Estrutura existente do Gradle

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] Dependências de HTTP client (webflux opcional), OpenAPI, Security, Logging, Observabilidade, Retry adicionadas
- [x] `./gradlew dependencies --configuration compileClasspath` resolve sem erro
- [x] Gate check passes: `./gradlew build`

**Tests**: none
**Gate**: build

---

### T2: Configurar logging estruturado (logback)

**What**: Criar logback-spring.xml com profile-based JSON/plain text logging
**Where**: `src/main/resources/logback-spring.xml`
**Depends on**: T1
**Reuses**: Padrão Spring Boot profile config

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] logback-spring.xml criado com appender JSON para prod e CONSOLE para dev
- [x] Inclui traceId/spanId do MDC
- [x] Gate check passes: `./gradlew build`

**Tests**: none
**Gate**: build

---

### T3: Configurar observabilidade (Actuator, Prometheus, Tracing)

**What**: Atualizar application.yml com endpoints do Actuator, métricas Prometheus e tracing
**Where**: `src/main/resources/application.yml`, `application-prod.yml`, `application-dev.yml`
**Depends on**: T1, T2
**Reuses**: application.yml existente

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] Actuator expõe health, info, metrics, prometheus
- [x] Health checks configurados (db, diskSpace)
- [x] Tracing habilitado com Brave
- [x] Gate check passes: `./gradlew build`

**Tests**: none
**Gate**: build

---

### T4: Implementar clientes HTTP padronizados [P]

**What**: Criar configuração de RestClient com timeouts, retry e logging
**Where**: `src/main/java/com/example/starterkit/config/http/HttpClientConfig.java`, `HttpClientProperties.java`
**Depends on**: T3
**Reuses**: Spring Boot @ConfigurationProperties, RestClient.Builder

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] RestClient bean configurado com timeouts via properties
- [x] Retry configurado com Spring Retry (3 tentativas, backoff exponencial)
- [x] Logging de request/response com traceId
- [x] Teste unitário: HttpClientConfigTest verifica bean creation e properties
- [x] Gate check passes: `./gradlew test`
- [x] Test count: 2 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

---

### T5: Implementar OpenAPI/Swagger [P]

**What**: Configurar springdoc-openapi com informações da API via properties
**Where**: `src/main/java/com/example/starterkit/config/openapi/OpenApiConfig.java`
**Depends on**: T3
**Reuses**: Spring Boot @ConfigurationProperties

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] OpenApiConfig bean criado customizando título, versão, descrição
- [x] application.yml com propriedades da API
- [x] Teste unitário: OpenApiConfigTest verifica bean wiring
- [x] Gate check passes: `./gradlew test`
- [x] Test count: 1 test pass (no silent deletions)

**Tests**: unit
**Gate**: quick

---

### T6: Implementar segurança mínima [P]

**What**: Configurar Spring Security com HTTP Basic para actuator/admin e public para health/docs
**Where**: `src/main/java/com/example/starterkit/config/security/SecurityConfig.java`
**Depends on**: T3
**Reuses**: Spring Security filter chain pattern

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] SecurityFilterChain configura paths públicos e protegidos
- [x] UserDetailsService com usuário configurável via env/properties
- [x] Teste de integração: SecurityConfigTest verifica 401 em actuator e 200 em health
- [x] Gate check passes: `./gradlew test`
- [x] Test count: 4 tests pass (no silent deletions)

**Tests**: integration
**Gate**: full

---

### T7: Implementar tratamento global de erros [P]

**What**: Criar @ControllerAdvice com handlers para exceptions comuns
**Where**: `src/main/java/com/example/starterkit/error/GlobalExceptionHandler.java`
**Depends on**: T3
**Reuses**: Spring ProblemDetail (RFC 7807)

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] Handler para Exception genérica retornando ProblemDetail
- [x] Handler para MethodArgumentNotValid com field errors
- [x] Handler para NoHandlerFoundException
- [x] Inclui traceId no body de erro
- [x] Stack trace omitido em prod
- [x] Teste unitário (MockMvc): GlobalExceptionHandlerTest cobre cada handler
- [x] Gate check passes: `./gradlew test`
- [x] Test count: 3 tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick

---

### T8: Verificar integração entre features

**What**: Garantir que todas as features funcionam juntas sem conflitos
**Where**: Testes de integração
**Depends on**: T4, T5, T6, T7
**Reuses**: AbstractIntegrationTest

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] Teste de integração: app sobe com todas as configs
- [x] Actuator health retorna UP com todos os componentes
- [x] Security não bloqueia endpoints públicos
- [x] Gate check passes: `./gradlew test`
- [x] Test count: 2 tests pass (no silent deletions)

**Tests**: integration
**Gate**: full

---

### T9: Validação final e ajustes

**What**: Rodar build completo, verificar coverage e resolver conflitos
**Where**: Todos os arquivos modificados
**Depends on**: T8
**Reuses**: N/A

**Tools**:

- MCP: NONE
- Skill: tlc-spec-driven (Verifier)

**Done when**:

- [x] `./gradlew build` passa (compilação + todos os testes)
- [ ] Verifier valida todos os specs
- [ ] Documentação (README) atualizada com novas features

**Tests**: none
**Gate**: build

---

## Parallel Execution Map

```
Phase 1 (Sequential):
  T1 ──→ T2 ──→ T3

Phase 2 (Parallel):
  T3 complete, then:
    ├── T4 [P] — HTTP Client
    ├── T5 [P] — OpenAPI
    ├── T6 [P] — Security
    └── T7 [P] — Error Handler

Phase 3 (Sequential):
  T4, T5, T6, T7 complete, then:
    T8 ──→ T9
```

---

## Task Granularity Check

| Task | Scope | Status |
| ---- | ----- | ------ |
| T1: Adicionar dependências | 1 file (build.gradle.kts) | ✅ Granular |
| T2: Configurar logging | 1 file (logback-spring.xml) | ✅ Granular |
| T3: Configurar observabilidade | 3 files (application*.yml) | ✅ Granular |
| T4: HTTP Client | 2 files + tests | ✅ Granular |
| T5: OpenAPI | 1 file + tests | ✅ Granular |
| T6: Security | 1 file + tests | ✅ Granular |
| T7: Error Handler | 1 file + tests | ✅ Granular |
| T8: Integração | testes apenas | ✅ Granular |
| T9: Validação final | build + docs | ✅ Granular |

---

## Diagram-Definition Cross-Check

| Task | Depends On (task body) | Diagram Shows | Status |
| ---- | ---------------------- | ------------- | ------ |
| T1 | None | None | ✅ Match |
| T2 | T1 | T1 → T2 | ✅ Match |
| T3 | T1, T2 | T2 → T3 | ✅ Match |
| T4 | T3 | T3 → T4 | ✅ Match |
| T5 | T3 | T3 → T5 | ✅ Match |
| T6 | T3 | T3 → T6 | ✅ Match |
| T7 | T3 | T3 → T7 | ✅ Match |
| T8 | T4, T5, T6, T7 | T4,T5,T6,T7 → T8 | ✅ Match |
| T9 | T8 | T8 → T9 | ✅ Match |

---

## Test Co-location Validation

| Task | Code Layer Created/Modified | Matrix Requires | Task Says | Status |
| ---- | --------------------------- | --------------- | --------- | ------ |
| T1 | Config / YAML | none | none | ✅ OK |
| T2 | Config / YAML | none | none | ✅ OK |
| T3 | Config / YAML | none | none | ✅ OK |
| T4 | Config / Bean | unit | unit | ✅ OK |
| T5 | Config / Bean | unit | unit | ✅ OK |
| T6 | Security | integration | integration | ✅ OK |
| T7 | Error Handler | unit | unit | ✅ OK |
| T8 | Integration | integration | integration | ✅ OK |
| T9 | N/A | none | none | ✅ OK |
