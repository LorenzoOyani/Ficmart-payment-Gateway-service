package com.org.domain.model;

public record IdempotencyKey(
        String idemKey,
        String endpoint,
        String requestHash,
        String responseBody,
        String responseStatus

) {

}
