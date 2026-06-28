# Template Infrastructure Design

**Spec**: Múltiplas specs em `.specs/features/*`
**Status**: Draft

---

## Architecture Overview

Este design cobre 6 features de infraestrutura para o template Spring Boot:
1. Clientes HTTP padronizados
2. OpenAPI/Swagger
3. Segurança mínima
4. Logging estruturado
5. Observabilidade (Actuator, métricas, tracing)
6. Tratamento global de erros

Todas as features são **independentes** e podem ser implementadas em paralelo, com exceção de Observabilidade que depende de Logging para trace IDs.

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
├──────────┬──────────┬──────────┬──────────┬─────────────────┤
│  HTTP    │ OpenAPI  │ Security │  Error   │ Observability   │
│ Client   │ /Swagger │  (Basic) │ Handler  │ (Actuator +     │
│ Config   │ Config   │  Config  │          │  Micrometer)    │
├──────────┴──────────┴──────────┴──────────┴─────────────────┤
│                    Logging (Logback JSON)                    │
└─────────────────────────────────────────────────────────────┘
```

---

## Code Reuse Analysis

### Existing Components to Leverage

| Component | Location | How to Use |
|-----------|----------|------------|
| HealthCheckController | `health/HealthCheckController.java` | Referência para padrão de controller |
| application.yml | `resources/application*.yml` | Extender com novas properties |
| AbstractIntegrationTest | `AbstractIntegrationTest.java` | Base para novos testes de integração |

---

## Components

### 1. HTTP Client Config

- **Purpose**: Fornecer RestClient e WebClient configurados com timeouts, retry e logging
- **Location**: `com.example.starterkit.config.http.HttpClientConfig`
- **Interfaces**:
  - `RestClient restClient()` — bean com timeouts e observability
  - `WebClient webClient()` — bean opcional (condicional)
- **Dependencies**: `HttpClientProperties` (record de configuração)
- **Reuses**: Spring Boot auto-configuration patterns

### 2. OpenAPI Config

- **Purpose**: Configurar springdoc-openapi com informações da API
- **Location**: `com.example.starterkit.config.openapi.OpenApiConfig`
- **Interfaces**:
  - `OpenAPI customOpenAPI()` — bean customizando título, versão, descrição
- **Dependencies**: `springdoc-openapi-starter-webmvc-ui`
- **Reuses**: Spring Boot properties binding

### 3. Security Config

- **Purpose**: Proteger endpoints sensíveis com HTTP Basic
- **Location**: `com.example.starterkit.config.security.SecurityConfig`
- **Interfaces**:
  - `SecurityFilterChain filterChain(HttpSecurity)` — define regras de acesso
  - `UserDetailsService userDetailsService()` — usuário admin configurável
- **Dependencies**: `SecurityProperties` (usuário/senha)
- **Reuses**: Spring Security filter chain padrão

### 4. Logging Config

- **Purpose**: Configurar Logback para JSON em prod, plain text em dev
- **Location**: `logback-spring.xml` em resources
- **Interfaces**: N/A — configuração declarativa
- **Dependencies**: `logstash-logback-encoder`
- **Reuses**: Spring Boot profile-based config

### 5. Observability Config

- **Purpose**: Configurar Actuator, Micrometer, Prometheus e Tracing
- **Location**: `application*.yml` (properties) + dependências Gradle
- **Interfaces**: N/A — configuração via properties
- **Dependencies**: `micrometer-registry-prometheus`, `micrometer-tracing-bridge-brave`
- **Reuses**: Spring Boot Actuator auto-config

### 6. Global Error Handler

- **Purpose**: Capturar exceptions e retornar respostas padronizadas
- **Location**: `com.example.starterkit.error.GlobalExceptionHandler`
- **Interfaces**:
  - `@ExceptionHandler` methods para cada tipo de exception
- **Dependencies**: ProblemDetail (RFC 7807), HttpServletRequest
- **Reuses**: Spring `@ControllerAdvice` pattern

---

## Error Handling Strategy

| Error Scenario | Handling | User Impact |
|----------------|----------|-------------|
| Unhandled exception | GlobalExceptionHandler → 500 + ProblemDetail | JSON padronizado, sem stack trace em prod |
| Validation error | MethodArgumentNotValid → 400 + field errors | Lista de campos inválidos |
| Missing endpoint | NoHandlerFoundException → 404 | Mensagem clara |
| Security denied | Spring Security → 401/403 | Sem detalhes de stack trace |
| External API failure | RestClientException → propagated or wrapped | Custom exception com contexto |

---

## Risks & Concerns

| Concern | Impact | Mitigation |
|---------|--------|------------|
| Spring Security pode bloquear Swagger UI | Docs inacessíveis em dev | Whitelist `/swagger-ui/**` e `/v3/api-docs/**` em SecurityConfig |
| WebClient adiciona webflux ao classpath | Conflitos com MVC | Usar `@ConditionalOnClass` para WebClient bean |
| Logback JSON encoder adiciona dependência | Tamanho do JAR | Dependência runtime-only é aceitável |
| Actuator exposto em prod sem proteção | Vazamento de informações | Proteger `/actuator/**` (exceto health) com Basic Auth |

---

## Tech Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| RestClient over RestTemplate | RestClient | API moderna do Spring 6.1+, functional style |
| springdoc-openapi | springdoc-openapi-starter-webmvc-ui 2.6.x | Padrão da comunidade, suporta OpenAPI 3 |
| Security: HTTP Basic | InMemoryUserDetailsManager | Mínimo viável; JWT seria over-engineering |
| Logging: logstash encoder | logstash-logback-encoder 8.0 | Formato ECS/JSON padrão para ELK |
| Tracing: Brave (Zipkin) | micrometer-tracing-bridge-brave | Padrão do Spring Boot 3 |
| Error format: ProblemDetail | RFC 7807 | Formato moderno, suportado nativamente pelo Spring 6 |

---

## Dependencies a Adicionar (Gradle)

```kotlin
// HTTP
implementation("org.springframework.boot:spring-boot-starter-webflux") // opcional para WebClient

// OpenAPI
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

// Security
implementation("org.springframework.boot:spring-boot-starter-security")
testImplementation("org.springframework.security:spring-security-test")

// Logging
implementation("net.logstash.logback:logstash-logback-encoder:8.0")

// Observability
implementation("io.micrometer:micrometer-registry-prometheus")
implementation("io.micrometer:micrometer-tracing-bridge-brave")
implementation("io.zipkin.reporter2:zipkin-reporter-brave")

// Retry
implementation("org.springframework.retry:spring-retry")
implementation("org.springframework:spring-aspects")
```
