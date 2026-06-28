# Logging Estruturado Specification

## Problem Statement

Logs em texto livre são difíceis de parsear, consultar e correlacionar em sistemas distribuídos. Logging estruturado (JSON) com trace IDs permite integração eficiente com ferramentas de observabilidade.

## Goals

- [ ] Configurar logs em formato JSON em produção
- [ ] Incluir traceId e spanId em todos os logs
- [ ] Preservar logs legíveis (plain text) em desenvolvimento
- [ ] Padronizar campos: timestamp, level, logger, message, traceId, spanId

## Out of Scope

| Feature | Reason |
|---------|--------|
| Shipping para ELK/Loki | Infraestrutura; apenas formato é responsabilidade da app |
| Log aggregation | Fora do escopo |
| Custom appenders | Logback padrão é suficiente |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
|----------------------|----------------|-----------|------------|
| Usar Logback com encoder JSON (logstash-logback-encoder) | Sim | Padrão do ecossistema Spring | y |
| Profile `dev` = plain text, `prod` = JSON | Sim | Legibilidade local, parseabilidade em produção | y |
| Incluir MDC com traceId/spanId do Micrometer Tracing | Sim | Integração nativa com Spring Boot 3 | y |

**Open questions:** none.

---

## User Stories

### P1: Logs estruturados em JSON

**User Story**: As an ops engineer, I want logs in JSON format so that I can query them efficiently in production.

**Why P1**: Base para observabilidade em produção.

**Acceptance Criteria**:

1. WHEN the application runs in `prod` profile THEN logs SHALL be emitted as JSON objects
2. WHEN a request is processed THEN every log line SHALL include traceId and spanId
3. WHEN the application runs in `dev` profile THEN logs SHALL be in plain text format
4. WHEN a log entry is emitted THEN it SHALL contain timestamp, level, logger, message fields

**Independent Test**: Iniciar app em ambos profiles; capturar stdout e verificar formato.

---

## Edge Cases

- WHEN MDC não tem traceId (fora de contexto de request) THEN traceId field SHALL be null/omitted

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
|----------------|-------|-------|--------|
| LOG-01 | P1 | Design | Pending |
| LOG-02 | P1 | Design | Pending |
| LOG-03 | P1 | Design | Pending |
| LOG-04 | P1 | Design | Pending |

---

## Success Criteria

- [ ] Logs em produção são parseáveis como JSON
- [ ] traceId/spanId presentes em logs de request
- [ ] Logs em dev são legíveis como texto
