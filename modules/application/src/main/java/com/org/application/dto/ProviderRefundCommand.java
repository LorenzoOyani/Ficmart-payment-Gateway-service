package com.org.application.dto;

public record ProviderRefundCommand(
        String providerAuthId,
        String orderId,
        String customerId,
        Long amountCents,
        String currency,
        String idempotencyKey
) {}
