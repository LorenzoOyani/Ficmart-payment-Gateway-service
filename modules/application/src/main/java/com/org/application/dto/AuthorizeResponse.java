package com.org.application.dto;

public record AuthorizeResponse(
        String providerTxnId,
        String status,
        String message
) {}

