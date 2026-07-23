package com.org.domain.model;

import com.org.domain.enums.PaymentStatus;
import com.org.domain.exception.PaymentStatusException;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;



@Getter
public class Payment {

    private final Integer id;
    private final String orderId;
    private final String customerId;
    private final String merchantId;

    private PaymentStatus paymentStatus;

    private final long amount;
    private long amountReceived;
    private long amountRefunded;

    private final String currency;

    private String stripeCustomerId;
    private String stripePaymentIntentId;
    private String stripeLatestChargeId;
    private String stripeLatestRefundId;
    private String stripeCheckoutSessionId;

    private String failureCode;
    private String failureMessage;

    private Instant authorizedAt;
    private Instant capturedAt;
    private Instant canceledAt;
    private Instant refundedAt;
    private Instant failedAt;

    private final Instant createdAt;
    private Instant updatedAt;

    private Payment(
            Integer id,
            String orderId,
            String customerId,
            String merchantId,
            PaymentStatus paymentStatus,
            long amount,
            long amountReceived,
            long amountRefunded,
            String currency,
            String stripeCustomerId,
            String stripePaymentIntentId,
            String stripeLatestChargeId,
            String stripeLatestRefundId,
            String stripeCheckoutSessionId,
            String failureCode,
            String failureMessage,
            Instant authorizedAt,
            Instant capturedAt,
            Instant canceledAt,
            Instant refundedAt,
            Instant failedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.orderId = requireNonBlank(orderId, "orderId");
        this.customerId = requireNonBlank(customerId, "customerId");
        this.merchantId = requireNonBlank(merchantId, "merchantId");
        this.paymentStatus = Objects.requireNonNull(paymentStatus, "paymentStatus must not be null");

        this.amount = requirePositive(amount);
        this.amountReceived = requireNotNegative(amountReceived, "amountReceived");
        this.amountRefunded = requireNotNegative(amountRefunded, "amountRefunded");

        if (amountRefunded > amount) {
            throw new IllegalArgumentException("amountRefunded cannot be greater than amount");
        }

        this.currency = requireNonBlank(currency, "currency").toLowerCase();

        this.stripeCustomerId = stripeCustomerId;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.stripeLatestChargeId = stripeLatestChargeId;
        this.stripeLatestRefundId = stripeLatestRefundId;
        this.stripeCheckoutSessionId = stripeCheckoutSessionId;

        this.failureCode = failureCode;
        this.failureMessage = failureMessage;

        this.authorizedAt = authorizedAt;
        this.capturedAt = capturedAt;
        this.canceledAt = canceledAt;
        this.refundedAt = refundedAt;
        this.failedAt = failedAt;

        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt cannot be before createdAt");
        }
    }

    public static Payment newPending(
            String customerId,
            String orderId,
            String merchantId,
            long amount,
            String currency
    ) {
        Instant now = Instant.now();

        return new Payment(
                null,
                orderId,
                customerId,
                merchantId,
                PaymentStatus.PENDING,
                amount,
                0,
                0,
                currency,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    public void attachStripePaymentIntent(
            String stripePaymentIntentId,
            String stripeCustomerId
    ) {
        if (this.stripePaymentIntentId != null &&
                !this.stripePaymentIntentId.equals(stripePaymentIntentId)) {
            throw new IllegalStateException("Payment different from Stripe PaymentIntent");
        }

        this.stripePaymentIntentId = requireNonBlank(stripePaymentIntentId, "stripePaymentIntentId");
        this.stripeCustomerId = stripeCustomerId;
        this.paymentStatus = PaymentStatus.AUTHORIZING;
        touch();
    }

    public void attachStripeCheckoutSession(String stripeCheckoutSessionId) {
        this.stripeCheckoutSessionId = requireNonBlank(stripeCheckoutSessionId, "stripeCheckoutSessionId");
        touch();
    }

    public void markAuthorized() {
        if (paymentStatus == PaymentStatus.AUTHORIZED) {
            return;
        }

        requireStatus(PaymentStatus.AUTHORIZING);

        this.paymentStatus = PaymentStatus.AUTHORIZED;
        this.authorizedAt = Instant.now();
        touch();
    }

    public void markCaptured(String stripeLatestChargeId, long amountReceived) {
        if (paymentStatus == PaymentStatus.CAPTURED ||
                paymentStatus == PaymentStatus.PARTIALLY_REFUNDED ||
                paymentStatus == PaymentStatus.REFUNDED) {
            return;
        }

        if (paymentStatus == PaymentStatus.CANCELED) {
            return;
        }

        this.stripeLatestChargeId = stripeLatestChargeId;
        this.amountReceived = requireNotNegative(amountReceived, "amountReceived");
        this.paymentStatus = PaymentStatus.CAPTURED;
        this.capturedAt = Instant.now();
        clearFailure();
        touch();
    }

    public void markFailed(String failureCode, String failureMessage) {
        if (paymentStatus == PaymentStatus.CAPTURED ||
                paymentStatus == PaymentStatus.PARTIALLY_REFUNDED ||
                paymentStatus == PaymentStatus.REFUNDED) {
            return;
        }

        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.paymentStatus = PaymentStatus.FAILED;
        this.failedAt = Instant.now();
        touch();
    }

    public void markCanceled() {
        if (paymentStatus == PaymentStatus.CAPTURED ||
                paymentStatus == PaymentStatus.PARTIALLY_REFUNDED ||
                paymentStatus == PaymentStatus.REFUNDED) {
            return;
        }

        this.paymentStatus = PaymentStatus.CANCELED;
        this.canceledAt = Instant.now();
        touch();
    }

    public void applyRefund(String stripeRefundId, long refundAmount) {
        if (refundAmount <= 0) {
            return;
        }

        if (paymentStatus != PaymentStatus.CAPTURED &&
                paymentStatus != PaymentStatus.PARTIALLY_REFUNDED &&
                paymentStatus != PaymentStatus.REFUNDED) {
            throw new PaymentStatusException("Only captured payments can be refunded");
        }

        this.stripeLatestRefundId = stripeRefundId;

        long newRefundedAmount = this.amountRefunded + refundAmount;

        if (newRefundedAmount >= this.amount) {
            this.amountRefunded = this.amount;
            this.paymentStatus = PaymentStatus.REFUNDED;
        } else {
            this.amountRefunded = newRefundedAmount;
            this.paymentStatus = PaymentStatus.PARTIALLY_REFUNDED;
        }

        this.refundedAt = Instant.now();
        touch();
    }

    private void requireStatus(PaymentStatus expectedStatus) {
        if (paymentStatus != expectedStatus) {
            throw new PaymentStatusException(
                    "Expected payment status " + expectedStatus + " but was " + paymentStatus
            );
        }
    }

    private void clearFailure() {
        this.failureCode = null;
        this.failureMessage = null;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static long requirePositive(long value) {
        if (value <= 0 ) {
            throw new IllegalArgumentException("amount" + " must be greater than zero");
        }
        return value;
    }

    private static long requireNotNegative(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
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

    public String merchantId() {
        return merchantId;
    }

    public PaymentStatus paymentStatus() {
        return paymentStatus;
    }

    public long amount() {
        return amount;
    }

    public long amountReceived() {
        return amountReceived;
    }

    public long amountRefunded() {
        return amountRefunded;
    }

    public String currency() {
        return currency;
    }

    public String stripeCustomerId() {
        return stripeCustomerId;
    }

    public String stripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public String stripeLatestChargeId() {
        return stripeLatestChargeId;
    }

    public String stripeLatestRefundId() {
        return stripeLatestRefundId;
    }

    public String stripeCheckoutSessionId() {
        return stripeCheckoutSessionId;
    }

    public String failureCode() {
        return failureCode;
    }

    public String failureMessage() {
        return failureMessage;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        Payment that = (Payment) obj;

        return Objects.equals(this.id, that.id)
                && Objects.equals(this.orderId, that.orderId)
                && Objects.equals(this.customerId, that.customerId)
                && Objects.equals(this.merchantId, that.merchantId)
                && this.paymentStatus == that.paymentStatus
                && this.amount == that.amount
                && this.amountReceived == that.amountReceived
                && this.amountRefunded == that.amountRefunded
                && Objects.equals(this.currency, that.currency)
                && Objects.equals(this.stripePaymentIntentId, that.stripePaymentIntentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                orderId,
                customerId,
                merchantId,
                paymentStatus,
                amount,
                amountReceived,
                amountRefunded,
                currency,
                stripePaymentIntentId
        );
    }

    @Override
    public String toString() {
        return "Payment[" +
                "id=" + id +
                ", orderId=" + orderId +
                ", customerId=" + customerId +
                ", merchantId=" + merchantId +
                ", paymentStatus=" + paymentStatus +
                ", amount=" + amount +
                ", amountReceived=" + amountReceived +
                ", amountRefunded=" + amountRefunded +
                ", currency=" + currency +
                ", stripePaymentIntentId=" + stripePaymentIntentId +
                ", stripeLatestChargeId=" + stripeLatestChargeId +
                ", stripeLatestRefundId=" + stripeLatestRefundId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ']';
    }
}
