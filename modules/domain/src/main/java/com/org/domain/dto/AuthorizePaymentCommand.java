package com.org.domain.dto;

public record AuthorizePaymentCommand(
        String orderId,
        String customerId,
        long amount,
        IdempotencyKey idempotencyKey) {
}
