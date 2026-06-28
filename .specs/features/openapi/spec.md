# OpenAPI/Swagger Specification

## Problem Statement

Documentação de API manual fica desatualizada rapidamente. Ter OpenAPI/Swagger gerado automaticamente a partir do código garante que a documentação esteja sempre sincronizada com a implementação.

## Goals

- [ ] Gerar documentação OpenAPI automaticamente a partir dos controllers
- [ ] Expor Swagger UI em ambiente de desenvolvimento
- [ ] Configurar informações básicas da API (título, versão, descrição)
- [ ] Suportar anotações Jakarta Validation para documentar constraints

## Out of Scope

| Feature | Reason |
|---------|--------|
| Autenticação no Swagger UI | Será tratada na feature de segurança |
| Exemplos customizados de request/response | Nice-to-have; pode ser adicionado depois |
| Exportação para formatos externos | Fora do escopo de um starter kit |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
|----------------------|----------------|-----------|------------|
| Usar springdoc-openapi | Sim | Padrão da comunidade Spring; suporta OpenAPI 3 | y |
| Swagger UI apenas em dev | Sim | Evita expor UI de documentação em produção | y |
| Path padrão /swagger-ui.html | Sim | Convenção do springdoc | y |

**Open questions:** none.

---

## User Stories

### P1: Documentação OpenAPI automática

**User Story**: As a developer, I want the API to be auto-documented so that consumers can discover endpoints without reading code.

**Why P1**: Essencial para qualquer API REST pública ou interna.

**Acceptance Criteria**:

1. WHEN the application starts in dev profile THEN Swagger UI SHALL be available at `/swagger-ui.html`
2. WHEN a controller endpoint is added with @RestController THEN it SHALL appear in the OpenAPI spec automatically
3. WHEN Jakarta Validation constraints are used (@NotNull, @Size, etc.) THEN they SHALL be reflected in the OpenAPI schema
4. WHEN the API info is configured in application.yml THEN the OpenAPI document SHALL display title, version and description

**Independent Test**: Start app in dev profile and curl `/v3/api-docs` and `/swagger-ui.html`; verify content.

---

## Edge Cases

- WHEN production profile is active THEN Swagger UI SHALL NOT be exposed (security)

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
|----------------|-------|-------|--------|
| OPENAPI-01 | P1 | Design | Pending |
| OPENAPI-02 | P1 | Design | Pending |
| OPENAPI-03 | P1 | Design | Pending |
| OPENAPI-04 | P1 | Design | Pending |

---

## Success Criteria

- [ ] `/swagger-ui.html` funciona em dev
- [ ] `/v3/api-docs` retorna spec válido em JSON
- [ ] Informações da API são configuráveis via properties
