package com.org.application.ports;

import com.org.domain.dto.IdempotencyKey;

import java.util.Map;

public record PaymentProviderAuthorizeCmd(
        String orderId,
        String customerId,
        long amount_cent,
        String currency,
        IdempotencyKey idempotencyKey,
        Map<String, Object> metadata

        ) {

}
