package com.org.application.dto;

public record RefundResponse(
        String transactionId,
        String refundId ,
        String status,
        String message) {

}
