package com.org.domain.dto;

public record AuthorizeRequest(
        String order_id,
        String customer_id,
        long amount_cents,
        String currency,
        Card card
) {
    public record Card(
            String number,
            String cvv,
            String expiry
    ) {
    }
}
