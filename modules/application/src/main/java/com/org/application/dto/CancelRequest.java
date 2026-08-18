package com.org.application.dto;

public record CancelRequest(String paymentId, String orderId, String customerId , String reason) {
}
