package com.org.domain.dto;

import com.org.domain.enums.IdempotencyStatus;

public record IdemKeyResponse(
        String idemKey,
        String endpoint,
        String requestHash,
        IdempotencyStatus responseStatus,
        String responseBodyJson
) {
}
