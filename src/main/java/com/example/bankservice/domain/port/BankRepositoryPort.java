package com.example.bankservice.domain.port;

import com.example.bankservice.domain.model.Bank;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port required by the domain to persist and read Bank aggregates.
 */
public interface BankRepositoryPort {

    Bank save(Bank bank);

    Optional<Bank> findById(UUID id);

    Optional<Bank> findByBic(String bic);

    /**
     * Simple list for the exercise; //TODO we can introduce filters/pagination ports.
     */
    List<Bank> findAll();

    void deleteById(UUID id);

    Optional<Bank> findByNameAndCountry(String name, String country);
}
