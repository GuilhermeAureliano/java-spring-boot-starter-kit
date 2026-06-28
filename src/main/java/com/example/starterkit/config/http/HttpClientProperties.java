package com.example.starterkit.config.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.http.client")
public record HttpClientProperties(
        int connectTimeoutSeconds,
        int readTimeoutSeconds,
        Retry retry
) {
    public HttpClientProperties {
        if (connectTimeoutSeconds <= 0) {
            connectTimeoutSeconds = 5;
        }
        if (readTimeoutSeconds <= 0) {
            readTimeoutSeconds = 10;
        }
        if (retry == null) {
            retry = new Retry(3, 1000, 2.0);
        }
    }

    public record Retry(int maxAttempts, long initialIntervalMs, double multiplier) {
        public Retry {
            if (maxAttempts <= 0) {
                maxAttempts = 3;
            }
            if (initialIntervalMs <= 0) {
                initialIntervalMs = 1000;
            }
            if (multiplier <= 1.0) {
                multiplier = 2.0;
            }
        }
    }
}
