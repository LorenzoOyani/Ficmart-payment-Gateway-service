package com.org.persistence.entities;

import com.org.domain.enums.PaymentStatus;
import com.org.domain.exception.PaymentStatusException;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    private String orderId;

    private String customerId;

    private String merchantId;

    private PaymentStatus paymentStatus;

    private long amount;

    private String currency;
    private String authorizationReference;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant authorizedAt;
    private Instant capturedAt;
    private Instant voidedAt;
    private Instant refundedAt;
    private Instant expiresAt;

    private int attemptCount;

    private int nextRetryAttempt;


    public Payment(
            UUID id,
            String merchantId,
            String customerId,
            String orderId,
            long amount,
            PaymentStatus paymentStatus,
            Instant createdAt

                   ) {

        this.id = id;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public Payment() {}

    public Payment newPending(UUID id,String merchantId,String orderId, String customerId, long amount, String currency , Instant timestamp) {

return  null;
    }

    public void markedAuthorized(){
        requireStatus(paymentStatus);
        this.paymentStatus = PaymentStatus.AUTHORIZED;
        this.authorizedAt = Instant.now();
    }

    private void requireStatus(PaymentStatus expectedStatus) {
        if (expectedStatus != paymentStatus) {
            throw new PaymentStatusException("unknown payment state!");
        }
    }

    public void markAuthorized(String authorizationReference, Instant authorizedAt, Instant expiresAt) {
        requireStatus(PaymentStatus.AUTHORIZING);
        this.authorizedAt = authorizedAt;
        this.expiresAt = expiresAt;
        this.paymentStatus = PaymentStatus.AUTHORIZED;
        this.authorizationReference = authorizationReference;
        update();
    }

    @PreUpdate
    public void update(){
        this.createdAt = Instant.now();
    }


}
