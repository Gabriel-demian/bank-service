package com.example.bankservice.infrastructure.repository;

import com.example.bankservice.infrastructure.repository.entity.BankJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BankRepository extends JpaRepository<BankJpaEntity, UUID> {
    Optional<BankJpaEntity> findByBic(String bic);
    Optional<BankJpaEntity> findByNameAndCountry(String name, String country);
    boolean existsByBic(String bic);
}
