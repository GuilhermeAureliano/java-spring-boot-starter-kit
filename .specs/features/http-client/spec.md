# Clientes HTTP Padronizados Specification

## Problem Statement

Quase todo projeto precisa consumir APIs externas. Sem um padrão, cada desenvolvedor implementa de forma diferente, gerando inconsistências, dificuldade de manutenção e falta de observabilidade nos clientes HTTP.

## Goals

- [ ] Fornecer um cliente HTTP padrão baseado em RestClient (síncrono) com configurações sensíveis de timeout, retry e logging
- [ ] Suportar também WebClient (reativo) de forma opcional
- [ ] Permitir configuração via application.yml (timeouts, retry, base URLs)
- [ ] Garantir que requests/responses sejam logados de forma estruturada

## Out of Scope

| Feature | Reason |
|---------|--------|
| Cliente HTTP customizado manual (sem RestClient/WebClient) | Usar abstrações do Spring é o padrão da comunidade |
| Load balancing de clientes HTTP | Fora do escopo de um starter kit |
| Cache de respostas HTTP | Pode ser adicionado sob demanda |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
|----------------------|----------------|-----------|------------|
| Usar RestClient (Spring 6.1+) como padrão | Sim | RestClient é a API moderna do Spring, mais simples que WebClient para casos síncronos | y |
| Manter WebClient disponível como bean opcional | Sim | Projetos reativos podem precisar; adicionar dependência `spring-boot-starter-webflux` no classpath opcionalmente | y |
| Timeouts padrão | connect=5s, read=10s | Sensato para a maioria das APIs; configurável via properties | y |
| Retry automático | 3 tentativas com backoff exponencial | Padrão do Spring Retry; configurável | y |

**Open questions:** none — all resolved or logged above.

---

## User Stories

### P1: Cliente HTTP base configurável

**User Story**: As a developer, I want a pre-configured HTTP client so that I can call external APIs with sensible defaults.

**Why P1**: Essencial para qualquer projeto que consuma APIs externas.

**Acceptance Criteria**:

1. WHEN a RestClient bean is injected THEN it SHALL have connect timeout of 5s and read timeout of 10s by default
2. WHEN application.yml defines custom timeouts THEN the RestClient SHALL use those values
3. WHEN a request fails with a transient error THEN the client SHALL retry up to 3 times with exponential backoff
4. WHEN a request/response is made THEN it SHALL be logged with traceId, method, URI, status and duration

**Independent Test**: Inject RestClient and call a MockServer endpoint; verify timeout and retry behavior.

---

## Edge Cases

- WHEN the external API returns 4xx THEN system SHALL NOT retry (idempotent errors only for 5xx and timeouts)
- WHEN all retries are exhausted THEN system SHALL throw a custom `ExternalServiceException` with context

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
|----------------|-------|-------|--------|
| HTTP-01 | P1 | Design | Pending |
| HTTP-02 | P1 | Design | Pending |
| HTTP-03 | P1 | Design | Pending |
| HTTP-04 | P1 | Design | Pending |

---

## Success Criteria

- [ ] Developer can inject `RestClient` and use it immediately with sensible defaults
- [ ] Timeouts and retries are configurable via `application.yml`
- [ ] All HTTP calls emit structured logs
