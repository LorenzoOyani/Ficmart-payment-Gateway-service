package com.org.persistence.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Table(name = "Idempotency_table")
@Entity
public class IdempotencyEntity {

    @Id
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    private String endpoint;

    @Column(name = "request_hash")
    private String requestHash;


    @Column(name = "response_body")
    private String responseBody;

    @Column(nullable = false)
    private String responseStatus;

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
