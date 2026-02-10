package com.org.persistence.entities;

import com.org.domain.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Table(name = "Idempotency_table",
        uniqueConstraints = @UniqueConstraint(name = "uq_idem", columnNames = {"idempotency_key", "endpoint"}),
        indexes = {
            @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_expired_at", columnList = "expired_at")
        }
)
@Entity
public class IdempotencyEntity {

    @Id
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "endpoint",nullable = false)
    private String endpoint;

    @Column(name = "request_hash")
    private String requestHash;


    @Column(name = "operation")
    private String operation;

    @Lob
    @Column(name = "response_body")
    private String responseBodyJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus idempotencyStatus;

    @Column(name = "created_at",  nullable = false)
    private Instant createdAt;

    @Column(name = "expired_at", nullable = false)
    private Instant expiresAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }


    @PreUpdate
    public void preUpdate() {
        expiresAt = Instant.now();
    }






}
