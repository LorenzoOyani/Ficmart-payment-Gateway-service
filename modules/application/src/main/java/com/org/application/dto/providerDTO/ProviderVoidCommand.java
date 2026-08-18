package com.org.application.dto.providerDTO;

public record ProviderVoidCommand(
        String providerAuthId,
        String orderId,
        String customerId,
        String idempotencyKey
) {}
