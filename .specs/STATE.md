# Estado do Projeto

## Decisões (AD-NNN)

| ID | Decision | Status | Date |
|----|----------|--------|------|
| AD-001 | Usar RestClient (Spring 6.1+) como padrão para HTTP clients | active | 2026-06-28 |
| AD-002 | Usar springdoc-openapi-starter-webmvc-ui para OpenAPI/Swagger | active | 2026-06-28 |
| AD-003 | Segurança mínima via Spring Security HTTP Basic (não JWT/OAuth2) | active | 2026-06-28 |
| AD-004 | Logging estruturado com logstash-logback-encoder (JSON em prod, plain em dev) | active | 2026-06-28 |
| AD-005 | Observabilidade com Actuator + Micrometer Prometheus + Brave tracing | active | 2026-06-28 |
| AD-006 | Tratamento global de erros com ProblemDetail (RFC 7807) | active | 2026-06-28 |
| AD-007 | @EnableConfigurationProperties na classe principal para todos os records de config | active | 2026-06-28 |

---

## Handoff

Última sessão: 2026-06-28
Feature concluída: Template Infrastructure (6 features)
Commits: e23a0b1..b396c24
Próxima etapa: Nenhuma — todas as tasks concluídas e validadas.

---

## Features em Andamento

Nenhuma.

---

## Features Concluídas

- [x] Clientes HTTP padronizados
- [x] OpenAPI/Swagger
- [x] Segurança mínima
- [x] Logging estruturado
- [x] Observabilidade
- [x] Tratamento global de erros
