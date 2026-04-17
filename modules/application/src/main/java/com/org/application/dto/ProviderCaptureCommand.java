package com.org.application.dto;

public record ProviderCaptureCommand(
        String providerAuthId,
        String orderId,
        String customerId,
        long amountCents,
        String currency,
        String authorizationReference,
        String idempotencyKey
) {}