package com.example.bankservice.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable DTO for Bank data exchange via REST.
 */
public record BankDto(
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
