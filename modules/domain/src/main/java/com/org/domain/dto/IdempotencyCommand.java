package com.org.domain.dto;

import java.time.Instant;
import java.util.Objects;

public record IdempotencyCommand(
        String Operation,
        String requestHash,
        String key,
        Instant expired_at
)
{

    public IdempotencyCommand {
        Objects.requireNonNull(Operation, "Operation");
        Objects.requireNonNull(requestHash, "requestHash");
        Objects.requireNonNull(key, "key");
    }

    public static IdempotencyCommand of(String operation,  String requestHash, String key, Instant expired_at) {
        return new IdempotencyCommand(operation, requestHash, key, expired_at);
    }
}
