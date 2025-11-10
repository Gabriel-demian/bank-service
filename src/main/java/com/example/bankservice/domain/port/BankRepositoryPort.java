package com.example.bankservice.domain.port;

import com.example.bankservice.domain.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing Bank entities.
 */
public interface BankRepositoryPort extends JpaRepository<Bank, UUID>, JpaSpecificationExecutor<Bank> {
    Optional<Bank> findByBic(String bic);
}
