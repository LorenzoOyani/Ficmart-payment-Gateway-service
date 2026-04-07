package com.org.application.dto;

import com.org.domain.dto.IdempotencyKey;

import java.util.Map;

public record PaymentProviderAuthorizeCmd(
        String orderId,
        String customerId,
        long amount_cent,
        String currency,
        String idempotencyKey,
        Map<String, Object> metadata

        ) {

}
