package com.org.domain.model;

import com.org.domain.enums.IdempotencyStatus;

import java.time.Instant;
import java.util.UUID;

public record IdempotencyRecord(
        UUID id,
         String merchantId,
        String operation,
        String idempotencyKey,
        String requestHash,
        IdempotencyStatus status,
        Integer responseCode,
        String responseBody,
        Instant lockedAt,
        Instant completedAt,
        Instant createdAt
) {


    public  void markedFailed(Integer responseCode, String responseBody) {
         new IdempotencyRecord(
                UUID.randomUUID(),
                merchantId,
                operation,
                idempotencyKey,
                requestHash,
                IdempotencyStatus.FAILED,
                responseCode,
                responseBody,
                Instant.now(),
                Instant.now(),
                Instant.now()

        );
    }

    public IdempotencyRecord inProgress(String merchantId, String idempotencyKey, String requestHash, String operation) {
        return new IdempotencyRecord(
                UUID.randomUUID(),
                merchantId,
                idempotencyKey,
                requestHash,
                operation,
                IdempotencyStatus.IN_PROGRESS,
                responseCode,
                responseBody,
                Instant.now(),
                Instant.now(),
                Instant.now()
        );
    }

    public void markCompleted(Integer responseCode, String responseBody) {
        new  IdempotencyRecord(
                UUID.randomUUID(),
                merchantId,
                idempotencyKey,
                requestHash,
                operation,
                IdempotencyStatus.COMPLETED,
                responseCode,
                responseBody,
                Instant.now(),
                Instant.now(),
                Instant.now()
        );
    }




}
