package com.org.domain.dto;

public record AuthorizeRequest(
        String order_id,
        String customer_id,
        long amount_cents,
        String currency
) {

    }

