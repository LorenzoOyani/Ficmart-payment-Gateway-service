package com.org.application.ports;

public record ProviderVoidCommand(
        String providerAuthId,
        String orderId,
        String customerId,
        String idempotencyKey
) {}
