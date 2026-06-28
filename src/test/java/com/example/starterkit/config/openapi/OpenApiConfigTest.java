package com.example.starterkit.config.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OpenApiConfig.class)
@TestPropertySource(properties = {
        "app.api.title=Test API",
        "app.api.version=2.0.0",
        "app.api.description=Test Description"
})
class OpenApiConfigTest {

    @Autowired
    private OpenApiConfig config;

    @Test
    void openApiBeanIsCreated() {
        assertThat(config).isNotNull();
    }
}
