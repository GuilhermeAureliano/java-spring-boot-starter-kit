package com.example.starterkit.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(ApiInfoProperties properties) {
        return new OpenAPI()
                .info(new Info()
                        .title(properties.title())
                        .version(properties.version())
                        .description(properties.description()));
    }

    @ConfigurationProperties(prefix = "app.api")
    public record ApiInfoProperties(
            String title,
            String version,
            String description
    ) {
        public ApiInfoProperties {
            if (title == null || title.isBlank()) {
                title = "Spring Boot Starter Kit API";
            }
            if (version == null || version.isBlank()) {
                version = "v1";
            }
            if (description == null || description.isBlank()) {
                description = "API documentation for Spring Boot Starter Kit";
            }
        }
    }
}
