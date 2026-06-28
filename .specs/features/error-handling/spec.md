# Tratamento Global de Erros Specification

## Problem Statement

Sem tratamento global de erros, exceptions não tratadas vazam stack traces para o cliente, retornam status codes inconsistentes e dificultam o debug. Um handler centralizado padroniza respostas de erro e melhora a experiência do consumidor da API.

## Goals

- [ ] Criar @ControllerAdvice para capturar exceptions globais
- [ ] Padronizar resposta de erro: timestamp, status, error, message, path, traceId
- [ ] Tratar exceptions comuns: ValidationException, MethodArgumentNotValid, NoHandlerFound, etc.
- [ ] Garantir que stack traces NÃO vazem em produção
- [ ] Preservar stack traces em desenvolvimento

## Out of Scope

| Feature | Reason |
|---------|--------|
| I18n de mensagens de erro | Pode ser adicionado depois |
| Error codes customizados por domínio | Nice-to-have; apenas estrutura base |
| Retry automático em erros | Feature de resiliência, não de tratamento de erro |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
|----------------------|----------------|-----------|------------|
| Estrutura de erro padrão do Spring ProblemDetail (RFC 7807) | Sim | Formato moderno e padronizado | y |
| Incluir traceId no body de erro | Sim | Facilita correlação com logs | y |
| Stack trace em dev apenas | Sim | Segurança em produção | y |

**Open questions:** none.

---

## User Stories

### P1: Handler global de erros

**User Story**: As an API consumer, I want consistent error responses so that I can handle failures programmatically.

**Why P1**: Essencial para qualquer API REST.

**Acceptance Criteria**:

1. WHEN an unhandled exception occurs THEN the response SHALL be a JSON with status, error, message, path and traceId
2. WHEN a @Valid validation fails THEN the response SHALL be 400 Bad Request with field errors
3. WHEN a request is made to a non-existent endpoint THEN the response SHALL be 404 with structured error
4. WHEN the app runs in prod profile THEN stack traces SHALL NOT be included in error responses
5. WHEN the app runs in dev profile THEN stack traces MAY be included in error responses

**Independent Test**: Fazer requests que geram erros; verificar formato e conteúdo da resposta.

---

## Edge Cases

- WHEN a required request parameter is missing THEN response SHALL be 400 with clear message
- WHEN request body is malformed JSON THEN response SHALL be 400 without exposing parser internals

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
|----------------|-------|-------|--------|
| ERR-01 | P1 | Design | Pending |
| ERR-02 | P1 | Design | Pending |
| ERR-03 | P1 | Design | Pending |
| ERR-04 | P1 | Design | Pending |
| ERR-05 | P1 | Design | Pending |

---

## Success Criteria

- [ ] Todas as exceptions retornam JSON padronizado
- [ ] Status codes corretos para cada tipo de erro
- [ ] Stack traces não vazam em produção
