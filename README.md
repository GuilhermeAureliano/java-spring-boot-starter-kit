# Spring Boot Starter Kit

Template reutilizável para projetos Java + Spring Boot com Gradle.

## Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.4.5
- **Gradle**: 8.13 (Kotlin DSL)
- **Spring AI**: 1.0.0-M7
- **Banco de dados**: H2 (dev) / PostgreSQL (prod)
- **Migrations**: Flyway

## Requisitos

- JDK 21

## Como usar

### Executar localmente (perfil dev)

```bash
./gradlew bootRun
```

A aplicação sobe em `http://localhost:8080` com banco H2 em memória.

### Health check

```bash
curl http://localhost:8080/actuator/health
```

### Build e testes

```bash
./gradlew build
./gradlew test
```

### Console H2 (dev)

Acesse `http://localhost:8080/h2-console`.

- JDBC URL: `jdbc:h2:mem:starterkit`
- User: `sa`
- Password: (deixe em branco)

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
- H2 (runtime dev)
- Spring AI OpenAI Starter
- Testcontainers (testes)

## Estrutura de pacotes

```
com.example.starterkit
├── SpringBootStarterKitApplication.java
└── item
    ├── Item.java
    ├── ItemController.java
    ├── ItemRepository.java
    ├── ItemRequest.java
    ├── ItemResponse.java
    └── ItemService.java
```

## Observações

- O projeto **não utiliza Lombok**. DTOs são implementados com `record` e entidades com getters/setters explícitos.
- A chave OpenAI no perfil `dev` é apenas um placeholder para permitir o startup. Configure `OPENAI_API_KEY` corretamente ao usar recursos de IA.
