# Segurança Mínima Specification

## Problem Statement

Projetos precisam de uma base de segurança desde o início. Implementar do zero é arriscado e demorado. Uma configuração mínima pronta acelera o desenvolvimento sem impor um sistema de auth complexo.

## Goals

- [ ] Proteger endpoints sensíveis (actuator, admin) com autenticação básica
- [ ] Permitir configuração de usuário/senha via environment variables
- [ ] Deixar endpoints públicos (health, API docs) abertos por padrão
- [ ] Adicionar headers de segurança (HSTS, X-Frame-Options, etc.) via Spring Security

## Out of Scope

| Feature | Reason |
|---------|--------|
| JWT/OAuth2 completo | Fora do escopo de "mínima"; pode ser adicionado depois |
| RBAC complexo | Apenas admin vs público por enquanto |
| Password encoding customizado | Usar BCrypt padrão |
| Session management | Stateless por padrão |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
|----------------------|----------------|-----------|------------|
| Spring Security como base | Sim | Padrão do ecossistema Spring | y |
| HTTP Basic para proteção mínima | Sim | Simples, efetivo para proteger actuator/admin | y |
| Usuário padrão `admin` com senha de env | Sim | Convenção; senha deve ser alterada em produção | y |
| Endpoints públicos: /api/health, /actuator/health, /swagger-ui/**, /v3/api-docs/** | Sim | Necessários para health checks e docs | y |

**Open questions:** none.

---

## User Stories

### P1: Segurança básica

**User Story**: As an ops engineer, I want sensitive endpoints protected so that unauthorized users cannot access internal data.

**Why P1**: Essencial para deploys em qualquer ambiente.

**Acceptance Criteria**:

1. WHEN an unauthenticated user accesses `/actuator/info` THEN system SHALL return 401 Unauthorized
2. WHEN an authenticated user (admin) accesses `/actuator/info` THEN system SHALL return 200 OK
3. WHEN a request to `/api/health` is made without credentials THEN system SHALL return 200 OK (public)
4. WHEN the application starts THEN security headers SHALL be present in all responses

**Independent Test**: Fazer requests com e sem Basic Auth; verificar status codes.

---

## Edge Cases

- WHEN no security user is configured THEN system SHALL log a warning and use a generated secure password
- WHEN wrong credentials are provided THEN system SHALL return 401 without leaking stack traces

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
|----------------|-------|-------|--------|
| SEC-01 | P1 | Design | Pending |
| SEC-02 | P1 | Design | Pending |
| SEC-03 | P1 | Design | Pending |
| SEC-04 | P1 | Design | Pending |

---

## Success Criteria

- [ ] Actuator endpoints protegidos por Basic Auth
- [ ] Public endpoints acessíveis sem autenticação
- [ ] Headers de segurança presentes em todas as respostas
