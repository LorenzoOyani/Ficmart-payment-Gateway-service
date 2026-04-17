package com.org.domain.dto;

import java.time.Instant;

public record PaymentAuthorizeResponse(
        String status,
        String providerTxnId,
        String externalTxnId,
        Instant createdAt,
        Instant expiresAt
) {
}
