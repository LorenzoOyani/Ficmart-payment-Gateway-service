package com.org.application.ports;

public record ProviderAuthorizeResponse(
        String providerTxnId,
        String status,      // APPROVED / DECLINED
        String message
) {}

