package com.example.bankservice.domain.service;

import com.example.bankservice.domain.model.Bank;
import com.example.bankservice.domain.port.BankRepositoryPort;
import com.example.bankservice.shared.exception.DuplicateResourceException;
import com.example.bankservice.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Validates input, enforces business rules.
 */
@Service
public class BankService {

    private static final Pattern BIC_PATTERN = Pattern.compile("^[A-Z0-9]{8}([A-Z0-9]{3})?$");
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("^[A-Z]{2}$");

    private final BankRepositoryPort repository;

    public BankService(BankRepositoryPort repository) {
        this.repository = repository;
    }

    /**
     * Create
     **/
    public Bank create(String name, String bic, String country, String routingNumber) {
        validateRequired(name, "name");
        validateRequired(bic, "bic");
        validateRequired(country, "country");
        validateBic(bic);
        validateCountry(country);

        repository.findByBic(bic).ifPresent(b -> {
            throw new DuplicateResourceException("BIC already exists: " + bic);
        });
        repository.findByNameAndCountry(name, country).ifPresent(b -> {
            throw new DuplicateResourceException("Bank with same name and country already exists: " + name + ", " + country);
        });

        Bank bank = Bank.newBank(name, bic, country, routingNumber);
        return repository.save(bank);
    }

    /**
     * Read
     **/
    public Bank get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found: " + id));
    }

    public List<Bank> list() {
        return repository.findAll();
    }

    /**
     * Update
     **/
    public Bank update(UUID id, String name, String bic, String country, String routingNumber, long expectedVersion) {
        validateRequired(name, "name");
        validateRequired(bic, "bic");
        validateRequired(country, "country");
        validateBic(bic);
        validateCountry(country);

        Bank existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found: " + id));

        if (existing.getVersion() != expectedVersion) {
            throw new IllegalStateException("Version conflict. Expected=" + expectedVersion + " Actual=" + existing.getVersion());
        }

        // Enforce uniqueness if bic/name/country changed
        if (!Objects.equals(existing.getBic(), bic)) {
            repository.findByBic(bic).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("BIC already exists: " + bic);
                }
            });
        }
        if (!Objects.equals(existing.getName(), name) || !Objects.equals(existing.getCountry(), country)) {
            repository.findByNameAndCountry(name, country).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("Bank with same name and country already exists: " + name + ", " + country);
                }
            });
        }

        existing.setName(name);
        existing.setBic(bic);
        existing.setCountry(country);
        existing.setRoutingNumber(routingNumber);
        existing.touchUpdatedAt();

        return repository.save(existing);
    }

    /**
     * Delete
     **/
    public void delete(UUID id) {
        // Ensure exists to return 404 if not
        repository.findById(id).orElseThrow(() -> new NotFoundException("Bank not found: " + id));
        repository.deleteById(id);
    }

    /**
     * Validation helpers
     **/
    private static void validateRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' is required");
        }
    }

    private static void validateBic(String bic) {
        if (!BIC_PATTERN.matcher(bic).matches()) {
            throw new IllegalArgumentException("BIC must be 8 or 11 uppercase alphanumeric characters");
        }
    }

    private static void validateCountry(String country) {
        if (!COUNTRY_PATTERN.matcher(country).matches()) {
            throw new IllegalArgumentException("Country must be ISO-3166-1 alpha-2 (e.g., AR, US)");
        }
    }
}
