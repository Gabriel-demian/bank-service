package com.example.bankservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Beans cross-cutting.
 */
@Configuration
public class PersistenceConfig {

    @Bean
    WebClient webClient(Environment env) {
        String port = env.getProperty("server.port", "8080");
        String base = env.getProperty("app.self-base-url", "http://localhost:" + port);
        return WebClient.builder().baseUrl(base).build();
    }

}
