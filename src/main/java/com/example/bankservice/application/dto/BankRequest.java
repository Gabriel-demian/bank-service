package com.example.bankservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for create/update operations.
 */
public record BankRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^[A-Z0-9]{8}([A-Z0-9]{3})?$") String bic,
        @NotBlank @Pattern(regexp = "^[A-Z]{2}$") String country,
        String routingNumber
) {}
