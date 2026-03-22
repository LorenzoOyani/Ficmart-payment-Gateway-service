package com.org.persistence.entities;

import com.org.domain.dto.PaymentResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;


import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
public class Idempotency_records {

    public enum Status { IN_PROGRESS, COMPLETED, FAILED }

    private UUID id;

    private String merchantId;

    private String operation;

    private String key;

    private String requestHash;

    private Status status;

    private String responseJson;

    private String errorMessage;

    private Instant createdAt;

    private Instant expiresAt;

    private Instant locked_At;

    protected Idempotency_records() {}

    public Idempotency_records(String merchantId, String operation, String key, String requestHash, Instant expiresAt, Instant locked_At) {
        this.id = UUID.randomUUID();
        this.merchantId = merchantId;
        this.operation = operation;
        this.key = key;
        this.requestHash = requestHash;
        this.status = Status.IN_PROGRESS;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
        this.locked_At = Instant.now();

    }

    public void markCompleted(String merchantId, PaymentResponse paymentResponse) {
        this.status = Status.COMPLETED;
        this.responseJson = responseJson;
    }

    public void markFailed(String errorMessage) {
        this.status = Status.FAILED;
        this.errorMessage = errorMessage;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }
}
