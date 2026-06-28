package com.example.starterkit.config.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;

@Configuration
@EnableRetry
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientConfig {

    private static final Logger log = LoggerFactory.getLogger(HttpClientConfig.class);

    @Bean
    public RestClient restClient(HttpClientProperties properties) {
        return RestClient.builder()
                .requestInterceptor(new LoggingInterceptor())
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout(Duration.ofSeconds(properties.connectTimeoutSeconds()));
                    setReadTimeout(Duration.ofSeconds(properties.readTimeoutSeconds()));
                }})
                .build();
    }

    static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            long start = System.currentTimeMillis();
            log.info("HTTP {} {}", request.getMethod(), request.getURI());
            try {
                ClientHttpResponse response = execution.execute(request, body);
                long duration = System.currentTimeMillis() - start;
                log.info("HTTP {} {} -> {} in {}ms", request.getMethod(), request.getURI(), response.getStatusCode(), duration);
                return response;
            } catch (IOException ex) {
                long duration = System.currentTimeMillis() - start;
                log.warn("HTTP {} {} -> ERROR in {}ms: {}", request.getMethod(), request.getURI(), duration, ex.getMessage());
                throw ex;
            }
        }
    }
}
