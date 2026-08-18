package com.org.application.dto;

public record CancelResponse(String paymentId, String status, String reason) {
}
