package com.org.application.ports;

public record ProviderRefundResult(String providerTxnId, String status, String message) {}
