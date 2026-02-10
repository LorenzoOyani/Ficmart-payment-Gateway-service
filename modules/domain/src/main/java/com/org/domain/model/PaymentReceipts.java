package com.org.domain.model;

import com.org.domain.enums.PaymentStatus;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
public class PaymentReceipts {

    private Long paymentRef;

    private String orderId;

    private String customerId;

    private long amountCents;

    private String currency;

    private PaymentStatus currentPaymentState;

    private String bankAuthorizationId;
    private String bankCaptureId;
    private String bankVoidId;
    private String bankRefundId;

    private Instant authorizedAt;
    private Instant capturedAt;
    private Instant voidedAt;
    private Instant refundedAt;


}


