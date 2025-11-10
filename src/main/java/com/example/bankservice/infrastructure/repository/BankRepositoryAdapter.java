package com.example.bankservice.infrastructure.repository;

import com.example.bankservice.domain.model.Bank;
import com.example.bankservice.domain.port.BankRepositoryPort;
import com.example.bankservice.infrastructure.repository.entity.BankJpaEntity;
import com.example.bankservice.infrastructure.repository.mapper.BankEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BankRepositoryAdapter implements BankRepositoryPort {

    private final SpringDataBankRepository jpa;
    private final BankEntityMapper mapper;

    public BankRepositoryAdapter(SpringDataBankRepository jpa, BankEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Bank save(Bank bank) {
        BankJpaEntity saved = jpa.save(mapper.toEntity(bank));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Bank> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Bank> findByBic(String bic) {
        return jpa.findByBic(bic).map(mapper::toDomain);
    }

    @Override
    public Optional<Bank> findByNameAndCountry(String name, String country) {
        return jpa.findByNameAndCountry(name, country).map(mapper::toDomain);
    }

    @Override
    public List<Bank> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
