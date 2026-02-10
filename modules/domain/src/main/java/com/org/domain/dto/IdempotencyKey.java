package com.org.domain.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IdempotencyKey {
    protected String idem_key;


    public IdempotencyKey(String idem_key) {
        this.idem_key = idem_key;
    }

    public void validate() {
        if (idem_key == null || idem_key.isEmpty()) {
            throw new IllegalArgumentException("idem_key is required");
        }
    }
}
