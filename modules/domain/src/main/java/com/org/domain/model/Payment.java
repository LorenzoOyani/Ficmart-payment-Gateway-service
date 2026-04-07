package com.org.domain.model;

import com.org.domain.enums.PaymentStatus;
import com.org.domain.exception.PaymentStatusException;

import java.time.Instant;
import java.util.Objects;

public class Payment {
    private final Integer id;
    private final String orderId;
    private final String customerId;
    private final String merchantId;
    private  PaymentStatus paymentStatus;
    private final long amount;
    private final String currency;
    private String authorizationReference;
    private String providerAuthorizationId;
    private String failureCode;
    private String failureMessage;
    private Instant authorizedAt;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant updatedAt;

    private Payment(
            Integer id,
            String orderId,
            String customerId,
            PaymentStatus paymentStatus,
            String authorizationReference,
            String providerAuthorizationId,
            long amount,
            String currency,
            String merchantId,
            Instant createdAt,
            Instant updatedAt

    ) {
        this.id = id;
        this.orderId = requireNonBlank(orderId, "orderId");
        this.customerId = requireNonBlank(customerId, "customerId");
        this.merchantId = merchantId;
        this.paymentStatus = Objects.requireNonNull(paymentStatus, "paymentStatus must not be null");
        this.amount = requirePositive(amount, "amount");
        this.providerAuthorizationId = requireNonBlank(providerAuthorizationId, "providerAuthorizationId");
        this.currency = requireNonBlank(currency, "currency").toUpperCase();
        this.authorizationReference = requireNonBlank(authorizationReference, "authorizationReference");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt cannot be before createdAt");
        }
    }

    public void markFailed(String code, String message) {
        this.failureCode = code;
        this.failureMessage = message;
        this.paymentStatus = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();

    }




    public void markAuthorized(String authorizationReference, Instant authorizedAt, Instant expiresAt) {
        requireStatus(PaymentStatus.AUTHORIZING);
        this.authorizedAt = authorizedAt;
        this.expiresAt = expiresAt;
        this.paymentStatus = PaymentStatus.AUTHORIZED;
        this.authorizationReference = authorizationReference;
        update();
    }


    private void requireStatus(PaymentStatus expectedStatus) {
        if (expectedStatus != paymentStatus) {
            throw new PaymentStatusException("unknown payment state!");
        }
    }

    public static Payment newPending(
            String customerId,
            String orderId,
            long amount,
            String currency,
            String merchantId
    ) {
        Instant now = Instant.now();

        return new Payment(
                null,
                orderId,
                customerId,
                PaymentStatus.PENDING,
                null,
                null,
                amount,
                currency,
                merchantId,
                now,
                now);
    }
    public void update(){
        this.createdAt = Instant.now();
    }

    public Integer id() {
        return id;
    }

    public String orderId() {
        return orderId;
    }

    public String customerId() {
        return customerId;
    }

    public PaymentStatus paymentStatus() {
        return paymentStatus;
    }

    public long amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    /// domain level invariants
    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static long requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Payment) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.orderId, that.orderId) &&
                Objects.equals(this.customerId, that.customerId) &&
                Objects.equals(this.paymentStatus, that.paymentStatus) &&
                this.amount == that.amount &&
                Objects.equals(this.currency, that.currency) &&
                Objects.equals(this.createdAt, that.createdAt) &&
                Objects.equals(this.updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, customerId, paymentStatus, amount, currency, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Payment[" +
                "id=" + id + ", " +
                "orderId=" + orderId + ", " +
                "customerId=" + customerId + ", " +
                "paymentStatus=" + paymentStatus + ", " +
                "amount=" + amount + ", " +
                "currency=" + currency + ", " +
                "createdAt=" + createdAt + ", " +
                "updatedAt=" + updatedAt + ']';
    }
}