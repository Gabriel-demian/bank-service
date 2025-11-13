package com.example.bankservice.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model for API responses (immutable).
 */
public record BankResponse(
        UUID id,
        String name,
        String bic,
        String country,
        String routingNumber,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
