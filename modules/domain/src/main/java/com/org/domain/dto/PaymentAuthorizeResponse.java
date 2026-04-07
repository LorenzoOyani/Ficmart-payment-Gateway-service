package com.org.domain.dto;

import java.time.Instant;

public record PaymentAuthorizeResponse(
        String status,
        String providerTxnId,
        Instant createdAt,
        Instant expiresAt
) {
}
