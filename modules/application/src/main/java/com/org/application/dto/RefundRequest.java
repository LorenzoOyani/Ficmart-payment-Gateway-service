package com.org.application.dto;

public record RefundRequest(String transactionId, String orderId, String customerId,String currency, long amount) {
}
