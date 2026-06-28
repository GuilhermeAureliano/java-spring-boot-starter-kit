buildscript {
    dependencies {
        // Flyway Gradle plugin scans its own classloader for the DatabaseType SPI;
        // put the PostgreSQL database plugin + JDBC driver on the buildscript
        // classpath so flywayValidate/flywayMigrate find a database handler.
        classpath("org.flywaydb:flyway-database-postgresql:10.20.1")
        classpath("org.postgresql:postgresql:42.7.5")
    }
}

plugins {
    java
    checkstyle
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    // Pinned to mirror Spring Boot 3.4.5's managed flyway-core (10.20.1);
    // a 12.x plugin would mix flyway-core versions and fail to load the DB plugin.
    id("org.flywaydb.flyway") version "10.20.1"
}

checkstyle {
    toolVersion = "10.18.1"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
}

flyway {
    url = System.getenv("FLYWAY_URL") ?: "jdbc:postgresql://localhost:5432/starterkit"
    user = System.getenv("FLYWAY_USER") ?: "postgres"
    password = System.getenv("FLYWAY_PASSWORD") ?: "postgres"
    locations = arrayOf("classpath:db/migration")
    // Only affects Gradle flywayValidate/flywayMigrate tasks (NOT the app's Spring
    // Flyway, which uses spring.flyway.*). Lets validate pass on a fresh/ephemeral
    // DB where all migrations are pending while still catching checksum/type drift
    // and missing-locally among applied migrations.
    ignoreMigrationPatterns = arrayOf("*:pending")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

extra["springAiVersion"] = "1.0.0-M7"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("org.postgresql:postgresql:42.7.5")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")

    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework:spring-aspects")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${extra["springAiVersion"]}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
