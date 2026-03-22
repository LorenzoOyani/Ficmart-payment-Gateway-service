package com.org.domain.model;

import com.org.domain.enums.PaymentStatus;

import java.time.Instant;
import java.util.Objects;

public class Payment {
    private final Integer id;
    private final String orderId;
    private final String customerId;
    private final PaymentStatus paymentStatus;
    private final long amount;
    private final String currency;
    private final Instant created_at;
    private final Instant updated_at;

    public Payment(
            Integer id,
            String orderId,
            String customerId,
            PaymentStatus paymentStatus,
            long amount,
            String currency,
            Instant created_at,
            Instant updated_at
    ) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.currency = currency;
        this.created_at = created_at;
        this.updated_at = updated_at;
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

    public Instant created_at() {
        return created_at;
    }

    public Instant updated_at() {
        return updated_at;
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
                Objects.equals(this.created_at, that.created_at) &&
                Objects.equals(this.updated_at, that.updated_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, customerId, paymentStatus, amount, currency, created_at, updated_at);
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
                "created_at=" + created_at + ", " +
                "updated_at=" + updated_at + ']';
    }


}
