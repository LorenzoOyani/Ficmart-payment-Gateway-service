package com.org.domain.model;

import com.org.domain.enums.PaymentStatus;

import java.time.Instant;

public record Payment(
        Integer id,
        String orderId,
        String customerId,
        PaymentStatus paymentStatus,
        long amount,
        String currency,
        Instant created_at,
        Instant updated_at
) {



//    public AuthorizePaymentResponse getPaymentStatus(PaymentStatus paymentStatus) {
//        return switch (paymentStatus){
//            case PENDING ->  new AuthorizePaymentResponse(orderId, customerId, PaymentStatus.PENDING, Instant.now());
//            case CAPTURED -> null;
//            case AUTHORIZED -> null;
//            case VOIDED -> null;
//            case REFUNDED -> null;
//        }
//    }
//
//    public record AuthorizePaymentResponse(String orderId, String customerId, PaymentStatus paymentStatus,
//                                            Instant now) {
//    }

//    Payment authorize()
}
