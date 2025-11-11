package com.example.bankservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Immutable DTO for Bank data exchange via REST.
 */
public record BankDto(
        UUID id,
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp = "^[A-Z0-9]{8}([A-Z0-9]{3})?$") String bic,
        @NotBlank
        @Pattern(regexp = "^[A-Z]{2}$") String country,
        String routingNumber
) {
}
