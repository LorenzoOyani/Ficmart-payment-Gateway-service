package com.org.application.dto.providerDTO;

import java.time.Instant;
import java.util.Map;

public record PaymentProviderAuthorizeCmd(
        String orderId,
        String customerId,
        long amount_cent,
        String currency,
        String idempotencyKey,
        Map<String, String> metadata,
        Instant instant

        ) {

}
