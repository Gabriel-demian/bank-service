package com.example.bankservice.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain model for a Bank.
 */
public class Bank {

    private UUID id;
    private long version;
    private String name;
    private String bic;
    private String country;
    private String routingNumber;
    private Instant createdAt;
    private Instant updatedAt;

    public Bank() {
        // empty for deserialization / mapping
    }

    public Bank(UUID id,
                long version,
                String name,
                String bic,
                String country,
                String routingNumber,
                Instant createdAt,
                Instant updatedAt) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.bic = bic;
        this.country = country;
        this.routingNumber = routingNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory helpers
     **/
    public static Bank newBank(String name, String bic, String country, String routingNumber) {
        Instant now = Instant.now();
        return new Bank(UUID.randomUUID(), 0L, name, bic, country, routingNumber, now, now);
    }

    public void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Equality by id (aggregate identity)
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bank)) return false;
        Bank bank = (Bank) o;
        return Objects.equals(id, bank.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
