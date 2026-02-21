package com.org.application.ports;

public record ProviderRefundCommand(
        String providerAuthId,   // refund by authId (full refund assumption)
        String orderId,
        String customerId,
        long amountCents,
        String currency,
        String idempotencyKey
) {}
