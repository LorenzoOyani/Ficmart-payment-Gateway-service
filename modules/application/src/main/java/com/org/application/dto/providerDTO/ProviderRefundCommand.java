package com.org.application.dto.providerDTO;

public record ProviderRefundCommand(
        String providerAuthId,
        String orderId,
        String customerId,
        Long amountCents,
        String currency,
        String idempotencyKey
) {}
