package com.org.application.dto;

public record ProviderRefundCommand(
        String providerAuthId,
        String orderId,
        String customerId,
        long amountCents,
        String currency,
        String idempotencyKey
) {}
