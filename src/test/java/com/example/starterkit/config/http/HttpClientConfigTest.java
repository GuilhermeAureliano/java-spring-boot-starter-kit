package com.example.starterkit.config.http;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = HttpClientConfig.class)
@TestPropertySource(properties = {
        "app.http.client.connect-timeout-seconds=3",
        "app.http.client.read-timeout-seconds=7"
})
@EnableConfigurationProperties(HttpClientProperties.class)
class HttpClientConfigTest {

    @Autowired
    private RestClient restClient;

    @Autowired
    private HttpClientProperties properties;

    @Test
    void restClientBeanIsCreated() {
        assertThat(restClient).isNotNull();
    }

    @Test
    void propertiesAreBound() {
        assertThat(properties.connectTimeoutSeconds()).isEqualTo(3);
        assertThat(properties.readTimeoutSeconds()).isEqualTo(7);
        assertThat(properties.retry().maxAttempts()).isEqualTo(3);
        assertThat(properties.retry().initialIntervalMs()).isEqualTo(1000);
        assertThat(properties.retry().multiplier()).isEqualTo(2.0);
    }
}
