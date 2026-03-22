package com.org.application.dto;

public record ProviderVoidCommand(
        String providerAuthId,
        String orderId,
        String customerId,
        String idempotencyKey
) {}
