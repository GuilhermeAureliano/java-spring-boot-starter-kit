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

## Dependências principais

- Spring Web
- Spring Data JPA
- Jakarta Validation
- Spring Boot Actuator
- Spring Boot DevTools
- Flyway
- PostgreSQL Driver
- Spring AI OpenAI Starter
- Testcontainers (testes)

## Estrutura de pacotes

```
com.example.starterkit
├── SpringBootStarterKitApplication.java
└── health
    └── HealthCheckController.java
```
