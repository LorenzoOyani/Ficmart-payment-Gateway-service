package com.org.domain.dto;

import com.org.domain.enums.PaymentStatus;

public record PaymentRequest(Integer id, String orderId, String customerId, long amount, PaymentStatus paymentStatus,String providerId) {
}
