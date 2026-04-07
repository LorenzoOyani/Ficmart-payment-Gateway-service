package com.org.domain.dto;

import java.util.Map;

public record PaymentAuthorizeCommand(
        String orderId,
        String customerId,
        long amount,
        IdempotencyKey idempotencyKey,
        String currency,
        Map<String, String> metadata


) {
}
