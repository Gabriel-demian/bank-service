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
    public WebClient webClient(@Value("${server.port:8080}") int port) {
        // baseUrl al mismo servicio (self-call)
        return WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }
}
