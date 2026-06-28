# Observabilidade Specification

## Problem Statement

Sem visibilidade de métricas, health checks e tracing, é impossível operar um serviço em produção de forma confiável. O Spring Boot fornece boas bases, mas precisam ser configuradas adequadamente.

## Goals

- [ ] Configurar Actuator com endpoints de health, metrics, info, prometheus
- [ ] Adicionar health checks customizados (banco de dados, espaço em disco)
- [ ] Expor métricas no formato Prometheus
- [ ] Habilitar Micrometer Tracing com Brave/Wavefront
- [ ] Configurar tracing para propagar trace IDs entre requests

## Out of Scope

| Feature | Reason |
|---------|--------|
| Dashboard Grafana pronto | Infraestrutura; apenas exposição de métricas |
| Alertmanager rules | Fora do escopo |
| Distributed tracing backend (Zipkin/Jaeger) | Apenas exportação; backend é infra |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
|----------------------|----------------|-----------|------------|
| Actuator endpoints expostos: health, info, metrics, prometheus | Sim | Conjunto mínimo para operação | y |
| Health check do banco via DataSourceHealthIndicator | Sim | Já vem com Spring Boot | y |
| Micrometer Tracing com Brave (Zipkin) | Sim | Padrão do Spring Boot 3 | y |
| Expor métricas para Prometheus em `/actuator/prometheus` | Sim | Formato padrão de métricas | y |

**Open questions:** none.

---

## User Stories

### P1: Actuator e métricas configurados

**User Story**: As an ops engineer, I want health checks and metrics exposed so that I can monitor the application.

**Why P1**: Essencial para qualquer deploy em produção.

**Acceptance Criteria**:

1. WHEN `/actuator/health` is called THEN it SHALL return UP with components (db, diskSpace, ping)
2. WHEN `/actuator/prometheus` is called THEN it SHALL return metrics in Prometheus format
3. WHEN `/actuator/info` is called THEN it SHALL return app name and version
4. WHEN a request is made to any endpoint THEN a timer metric SHALL be recorded by Spring MVC

**Independent Test**: Curl nos endpoints do actuator; verificar conteúdo.

### P2: Distributed tracing

**User Story**: As a developer, I want trace IDs propagated so that I can correlate logs and traces across services.

**Why P2**: Importante para debug em sistemas distribuídos.

**Acceptance Criteria**:

1. WHEN a request is received THEN a traceId SHALL be generated or extracted from headers
2. WHEN a downstream HTTP call is made THEN the traceId SHALL be propagated via B3 headers
3. WHEN logs are emitted during a request THEN they SHALL include the traceId

**Independent Test**: Fazer request com header traceparent; verificar se traceId aparece nos logs.

---

## Edge Cases

- WHEN database is unavailable THEN health check SHALL report DOWN for db component
- WHEN disk space is below threshold THEN health check SHALL report DOWN for diskSpace

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
|----------------|-------|-------|--------|
| OBS-01 | P1 | Design | Pending |
| OBS-02 | P1 | Design | Pending |
| OBS-03 | P1 | Design | Pending |
| OBS-04 | P1 | Design | Pending |
| OBS-05 | P2 | Design | Pending |
| OBS-06 | P2 | Design | Pending |
| OBS-07 | P2 | Design | Pending |

---

## Success Criteria

- [ ] `/actuator/health` mostra status detalhado
- [ ] `/actuator/prometheus` exporta métricas válidas
- [ ] traceId é propagado em requests HTTP
- [ ] Logs incluem traceId/spanId
