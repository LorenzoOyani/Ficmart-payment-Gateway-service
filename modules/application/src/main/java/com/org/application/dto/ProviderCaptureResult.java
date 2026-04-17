package com.org.application.dto;

public record ProviderCaptureResult(String providerTxnId, String status, String message, java.time.Instant instant) {}
