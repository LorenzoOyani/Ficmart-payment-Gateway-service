package com.org.domain.dto;

public record AuthorizePaymentCommand(
        String orderId,
        String customerId,
        long amount,
        IdempotencyKey idempotencyKey,
        String cvv,
        String cardNumber,
        int expiryMonth,
        int expiryYear


) {
}
