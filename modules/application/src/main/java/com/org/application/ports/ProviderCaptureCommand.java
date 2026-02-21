package com.org.application.ports;

public record ProviderCaptureCommand(
        String providerAuthId,   // bank authId
        String orderId,
        String customerId,
        long amountCents,
        String currency,
        String idempotencyKey
) {}