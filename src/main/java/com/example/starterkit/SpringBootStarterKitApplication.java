package com.example.starterkit;

import com.example.starterkit.config.http.HttpClientProperties;
import com.example.starterkit.config.security.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        HttpClientProperties.class,
        SecurityConfig.SecurityProperties.class
})
public class SpringBootStarterKitApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarterKitApplication.class, args);
    }
}
