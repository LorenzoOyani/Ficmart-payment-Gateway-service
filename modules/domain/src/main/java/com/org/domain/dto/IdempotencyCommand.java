package com.org.domain.dto;

import java.time.Instant;
import java.util.Objects;

public record IdempotencyCommand(
        String Operation,
        String requestHash,
        String key,
        Instant expired_at,
        String merchantId
)
{

    public IdempotencyCommand {
        Objects.requireNonNull(Operation, "Operation");
        Objects.requireNonNull(requestHash, "requestHash");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(expired_at, "expired_at");
        Objects.requireNonNull(merchantId, "merchantId");
    }

    public static IdempotencyCommand of(String operation,  String requestHash, String key, Instant expired_at, String merchantId) {
        return new IdempotencyCommand(operation, requestHash, key, expired_at, merchantId);
    }
}
