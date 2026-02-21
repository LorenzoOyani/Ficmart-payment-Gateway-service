package com.org.domain.dto;

public record BankCaptureResult(Boolean success, String tranId, String reason) {
}
