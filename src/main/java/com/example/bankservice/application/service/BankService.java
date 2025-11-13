package com.example.bankservice.application.service;

import com.example.bankservice.application.dto.BankDto;
import com.example.bankservice.application.mapper.BankAppMapper;
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
    private final BankAppMapper mapper;

    public BankService(BankRepositoryPort repository, BankAppMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // CREATE
    public BankDto create(BankDto dto) {
        String name = dto.name();
        String bic = dto.bic();
        String country = dto.country();
        String routingNumber = dto.routingNumber();

        validateRequired(name, "name");
        validateRequired(bic, "bic");
        validateRequired(country, "country");
        validateBic(bic);
        validateCountry(country);

        repository.findByBic(bic).ifPresent(b -> {
            throw new DuplicateResourceException("BIC already exists: " + bic);
        });
        repository.findByNameAndCountry(name, country).ifPresent(b -> {
            throw new DuplicateResourceException(
                    "Bank with same name and country already exists: " + name + ", " + country);
        });

        Bank bank = Bank.newBank(name, bic, country, routingNumber);
        Bank saved = repository.save(bank);
        return mapper.toDto(saved);
    }

    // READ
    public BankDto get(UUID id) {
        Bank bank = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found: " + id));
        return mapper.toDto(bank);
    }

    public List<BankDto> list(String country) {
        return repository.findAll().stream()
                .filter(b -> country == null || country.isBlank()
                        || country.equalsIgnoreCase(b.getCountry()))
                .map(mapper::toDto)
                .toList();
    }

    // UPDATE
    public BankDto update(UUID id, long expectedVersion, BankDto dto) {
        String name = dto.name();
        String bic = dto.bic();
        String country = dto.country();
        String routingNumber = dto.routingNumber();

        validateRequired(name, "name");
        validateRequired(bic, "bic");
        validateRequired(country, "country");
        validateBic(bic);
        validateCountry(country);

        Bank existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found: " + id));

        if (existing.getVersion() != expectedVersion) {
            throw new IllegalStateException("Version conflict. Expected=" + expectedVersion
                    + " Actual=" + existing.getVersion());
        }

        if (!Objects.equals(existing.getBic(), bic)) {
            repository.findByBic(bic).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("BIC already exists: " + bic);
                }
            });
        }
        if (!Objects.equals(existing.getName(), name)
                || !Objects.equals(existing.getCountry(), country)) {
            repository.findByNameAndCountry(name, country).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException(
                            "Bank with same name and country already exists: " + name + ", " + country);
                }
            });
        }

        existing.setName(name);
        existing.setBic(bic);
        existing.setCountry(country);
        existing.setRoutingNumber(routingNumber);
        existing.touchUpdatedAt();

        Bank saved = repository.save(existing);
        return mapper.toDto(saved);
    }

    // DELETE
    public void delete(UUID id) {
        repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found: " + id));
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
