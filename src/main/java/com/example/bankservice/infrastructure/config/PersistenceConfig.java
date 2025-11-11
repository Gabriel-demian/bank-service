package com.example.bankservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Beans cross-cutting.
 */
@Configuration
public class PersistenceConfig {

    @Bean
    WebClient webClient(@Value("${app.self-base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

}
