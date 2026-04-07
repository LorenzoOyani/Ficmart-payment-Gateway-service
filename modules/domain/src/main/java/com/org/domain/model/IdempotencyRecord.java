package com.org.domain.model;

import com.org.domain.enums.IdempotencyStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class IdempotencyRecord {
    private  UUID id;
    private  String merchantId;
    private  String operation;
    private  String idempotencyKey;
    private  String requestHash;
    private  IdempotencyStatus status;
    private static Integer responseCode;
    private static String responseBody;
    private  Instant lockedAt;
    private  Instant completedAt;
    private  Instant createdAt;

    public IdempotencyRecord(
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
        this.id = id;
        this.merchantId = merchantId;
        this.operation = operation;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = status;
        IdempotencyRecord.responseCode = responseCode;
        IdempotencyRecord.responseBody = responseBody;
        this.lockedAt = lockedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
    }

    public IdempotencyRecord(){}

    public void markedFailed(Integer responseCode, String responseBody) {
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

    public static IdempotencyRecord inProgress(String merchantId, String idempotencyKey, String requestHash, String operation) {
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
        new IdempotencyRecord(
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

    public UUID id() {
        return id;
    }

    public String merchantId() {
        return merchantId;
    }

    public String operation() {
        return operation;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public String requestHash() {
        return requestHash;
    }

    public IdempotencyStatus status() {
        return status;
    }

    public Integer responseCode() {
        return responseCode;
    }

    public String responseBody() {
        return responseBody;
    }

    public Instant lockedAt() {
        return lockedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (IdempotencyRecord) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.merchantId, that.merchantId) &&
                Objects.equals(this.operation, that.operation) &&
                Objects.equals(this.idempotencyKey, that.idempotencyKey) &&
                Objects.equals(this.requestHash, that.requestHash) &&
                Objects.equals(this.status, that.status) &&
                Objects.equals(this.responseCode, that.responseCode) &&
                Objects.equals(this.responseBody, that.responseBody) &&
                Objects.equals(this.lockedAt, that.lockedAt) &&
                Objects.equals(this.completedAt, that.completedAt) &&
                Objects.equals(this.createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, merchantId, operation, idempotencyKey, requestHash, status, responseCode, responseBody, lockedAt, completedAt, createdAt);
    }

    @Override
    public String toString() {
        return "IdempotencyRecord[" +
                "id=" + id + ", " +
                "merchantId=" + merchantId + ", " +
                "operation=" + operation + ", " +
                "idempotencyKey=" + idempotencyKey + ", " +
                "requestHash=" + requestHash + ", " +
                "status=" + status + ", " +
                "responseCode=" + responseCode + ", " +
                "responseBody=" + responseBody + ", " +
                "lockedAt=" + lockedAt + ", " +
                "completedAt=" + completedAt + ", " +
                "createdAt=" + createdAt + ']';
    }


}
