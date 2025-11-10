package com.example.bankservice.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.UUID;

/**
 * Bank entity representing a financial institution.
 */
@Entity
@Table(
        name = "bank",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_bank_bic", columnNames = "bic"),
                @UniqueConstraint(name = "uq_bank_name_country", columnNames = {"name", "country"})
        }
)
public class Bank {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Version
    @Column(nullable = false)
    private long version;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{8}([A-Z0-9]{3})?$", message = "BIC must be 8 or 11 uppercase alphanumeric characters")
    @Column(nullable = false, length = 11)
    private String bic;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a valid ISO-3166-1 alpha-2 code")
    @Column(nullable = false, length = 2)
    private String country;

    @Column(name = "routing_number", length = 50)
    private String routingNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Constructors **/
    public Bank() {
        // JPA default constructor
    }

    public Bank(UUID id, String name, String bic, String country, String routingNumber) {
        this.id = id;
        this.name = name;
        this.bic = bic;
        this.country = country;
        this.routingNumber = routingNumber;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /** Lifecycle hooks **/
    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /** Getters and Setters **/
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBic() { return bic; }
    public void setBic(String bic) { this.bic = bic; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getRoutingNumber() { return routingNumber; }
    public void setRoutingNumber(String routingNumber) { this.routingNumber = routingNumber; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
