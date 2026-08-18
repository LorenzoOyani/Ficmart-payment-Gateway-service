package com.org.application.dto;

public record CancelCommand(String paymentId,String idempotencyKey,  String reason) {
}
