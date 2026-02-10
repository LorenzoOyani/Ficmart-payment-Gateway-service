package com.org.persistence.entities;

import com.org.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name = "payment_receipts")
public class PaymentReceipts {


        @Id
        @Column(name="payment_ref", nullable=false)
        private Long paymentRef;

        @Column(name="order_id", nullable=false, unique = true)
        private String orderId;

        @Column(name="customer_id", nullable=false, unique = true)
        private String customerId;

        @Column(name="amount_cents", nullable=false)
        private long amountCents;

        @Column(nullable=false)
        private String currency;

        @Enumerated(EnumType.STRING)
        @Column(nullable=false)
        private PaymentStatus state;

        @Column(name="bank_authorization_id")
        private String bankAuthorizationId;
        @Column(name="bank_capture_id")
        private String bankCaptureId;
        @Column(name="bank_void_id")
        private String bankVoidId;
        @Column(name="bank_refund_id")
        private String bankRefundId;
        @Column(name="authorized_at")
        private Instant authorizedAt;
        @Column(name="captured_at")
        private Instant capturedAt;
        @Column(name="voided_at")
        private Instant voidedAt;
        @Column(name="refunded_at")
        private Instant refundedAt;

        @Version
        @Column(nullable=false)
        private int version;

        @Column(name="created_at", nullable=false) private Instant createdAt;
        @Column(name="updated_at", nullable=false) private Instant updatedAt;

        @PrePersist
        void prePersist() {
            createdAt = Instant.now();
            updatedAt = createdAt;
        }
        @PreUpdate
        void preUpdate() { updatedAt = Instant.now(); }

    }


