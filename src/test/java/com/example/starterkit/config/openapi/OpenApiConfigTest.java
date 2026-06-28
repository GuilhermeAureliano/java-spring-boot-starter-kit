package com.example.starterkit.config.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OpenApiConfig.class)
@EnableConfigurationProperties(OpenApiConfig.ApiInfoProperties.class)
@TestPropertySource(properties = {
        "app.api.title=Test API",
        "app.api.version=2.0.0",
        "app.api.description=Test Description"
})
class OpenApiConfigTest {

    @Autowired
    private OpenApiConfig.ApiInfoProperties properties;

    @Test
    void propertiesAreBound() {
        assertThat(properties.title()).isEqualTo("Test API");
        assertThat(properties.version()).isEqualTo("2.0.0");
        assertThat(properties.description()).isEqualTo("Test Description");
    }
}
