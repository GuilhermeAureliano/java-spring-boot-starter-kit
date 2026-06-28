# Spring Boot Starter Kit

Template reutilizável para projetos Java + Spring Boot com Gradle.

## Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.4.5
- **Gradle**: 8.13 (Kotlin DSL)
- **Spring AI**: 1.0.0-M7
- **Banco de dados**: PostgreSQL
- **Migrations**: Flyway
- **Testcontainers**: PostgreSQL (testes de integração)

## Requisitos

- JDK 21
- [SDKMAN](https://sdkman.io/) (opcional, recomendado)
- Docker & Docker Compose (opcional)

## Setup com SDKMAN

O projeto inclui um arquivo `.sdkmanrc` para garantir que você sempre use as versões corretas do Java e Gradle.

```bash
# Dentro da pasta do projeto
sdk env
```

> **Dica:** Para ativar automaticamente sempre que entrar na pasta:
> ```bash
> sdk config
> # Altere sdkman_auto_env para true
> ```

## Como usar

Escolha uma das três opções abaixo de acordo com a sua preferência:

---

### Opção 1: Projeto local + PostgreSQL local

1. Certifique-se de que o PostgreSQL está instalado e rodando localmente.
2. Crie o banco `starterkit`.
3. Execute a aplicação:

```bash
./gradlew bootRun
```

A aplicação sobe em `http://localhost:8080` e se conecta ao PostgreSQL local.

**Variáveis de ambiente (padrões do `application-dev.yml`):**

| Variável       | Valor padrão  |
|----------------|---------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/starterkit` |
| `DATABASE_USER`| `postgres`    |
| `DATABASE_PASSWORD`| `postgres` |
| `OPENAI_API_KEY`| `dummy-dev-key` |

---

### Opção 2: Projeto via Docker + PostgreSQL via Docker

Levanta toda a stack (aplicação + banco) com Docker Compose:

```bash
docker compose up --build
```

- Aplicação: `http://localhost:8080`
- PostgreSQL: porta `5432`

Para parar:

```bash
docker compose down
```

---

### Opção 3: Banco via Docker + Projeto localmente

Ideal para desenvolvimento local sem instalar o PostgreSQL na máquina:

1. Inicie apenas o banco:

```bash
docker compose up postgres -d
```

2. Execute a aplicação normalmente:

```bash
./gradlew bootRun
```

A aplicação se conectará ao PostgreSQL rodando no container.

Para parar o banco:

```bash
docker compose down
```

---

## Endpoints

### Health check

```bash
curl http://localhost:8080/api/health
```

Resposta:

```json
{
  "status": "UP",
  "timestamp": "2025-01-01T12:00:00Z"
}
```

### Actuator

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
```

## Build e testes

```bash
# Compilar e rodar testes
./gradlew build

# Apenas testes
./gradlew test
```

## Perfil de produção

No perfil `prod` a aplicação espera conexão com PostgreSQL via variáveis de ambiente:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/starterkit
export DATABASE_USER=starterkit
export DATABASE_PASSWORD=secret
export OPENAI_API_KEY=sua-chave-openai
./gradlew bootRun --args='--spring.profiles.active=prod'
```

---

## Build da imagem Docker

O Dockerfile utiliza multi-stage build com BuildKit cache, base image Eclipse Temurin Ubuntu, usuário não-root e tuning da JVM:

```bash
docker build -t starterkit:latest .
```

A imagem roda com:
- Heap limitado a 65% da memória do container (`-XX:MaxRAMPercentage=65`)
- GC logging habilitado (`-Xlog:gc*`)
- Usuário não-root (`UID 10001`)

---

## Kubernetes (exemplos)

Manifestos de referência estão em `k8s/`:

```bash
kubectl apply --dry-run=client -f k8s/
```

O `deployment.yaml` inclui:
- Probes: liveness, readiness e startup
- Requests/limits de CPU e memória
- Security context com `runAsNonRoot: true` e `runAsUser: 10001`

> **Nota:** Ajuste a imagem e as secrets de banco antes de aplicar em um cluster real.

---

## Dependências principais

- Spring Web
- Spring Data JPA
- Jakarta Validation
- Spring Boot Actuator
- Spring Security (mínima)
- Spring Boot DevTools
- Flyway
- PostgreSQL Driver
- Spring AI OpenAI Starter
- Testcontainers (testes)
- OpenAPI/Swagger (springdoc)
- Micrometer + Prometheus + Tracing
- Logstash Logback Encoder

## Infraestrutura incluída

### Clientes HTTP padronizados

`RestClient` configurado com timeouts, retry exponencial e logging estruturado:

```java
@Autowired
private RestClient restClient;
```

Configurável via `application.yml`:

```yaml
app:
  http:
    client:
      connect-timeout-seconds: 5
      read-timeout-seconds: 10
      retry:
        max-attempts: 3
        initial-interval-ms: 1000
        multiplier: 2.0
```

### OpenAPI / Swagger

Documentação automática gerada pelo springdoc. Acesse em dev:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Informações da API configuráveis via `application.yml`:

```yaml
app:
  api:
    title: "Minha API"
    version: "v1"
    description: "Descrição da API"
```

### Segurança mínima

Spring Security com HTTP Basic para proteger endpoints sensíveis:

- **Públicos**: `/api/health`, `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`
- **Protegidos**: `/actuator/**` (exceto health)

Credenciais configuráveis via variáveis de ambiente:

```bash
export APP_SECURITY_USERNAME=admin
export APP_SECURITY_PASSWORD=secret
```

### Logging estruturado

- **Dev**: logs em texto plano legível
- **Prod**: logs em JSON com `traceId` e `spanId`

Formato JSON (produção):

```json
{"@timestamp":"2025-01-01T12:00:00.000Z","level":"INFO","message":"...","traceId":"abc123","spanId":"def456"}
```

### Observabilidade

Actuator expõe:

- Health checks: `http://localhost:8080/actuator/health`
- Métricas Prometheus: `http://localhost:8080/actuator/prometheus`
- Info: `http://localhost:8080/actuator/info`

Tracing distribuído habilitado com Brave (Zipkin). Trace IDs propagados automaticamente entre requests HTTP.

### Tratamento global de erros

`@ControllerAdvice` padroniza respostas de erro no formato [RFC 7807](https://tools.ietf.org/html/rfc7807) (`ProblemDetail`):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/exemplo",
  "errors": {"campo": "mensagem"},
  "traceId": "abc123"
}
```

Stack traces não vazam em produção.

## Estrutura de pacotes

```
com.example.starterkit
├── SpringBootStarterKitApplication.java
├── config
│   ├── http
│   │   ├── HttpClientConfig.java
│   │   └── HttpClientProperties.java
│   ├── openapi
│   │   └── OpenApiConfig.java
│   └── security
│       └── SecurityConfig.java
├── error
│   └── GlobalExceptionHandler.java
└── health
    └── HealthCheckController.java
```
