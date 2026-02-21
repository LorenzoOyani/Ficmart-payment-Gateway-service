package com.org.persistence.entities;

import com.org.domain.enums.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderId;

    private String customerId;

    private PaymentStatus paymentStatus;

    private long amount;

    private String currency;

    // Provider refs
    private String providerAuthId;     // auth_<uuid>
    private String providerCaptureId;  // cap_<uuid>
    private String providerRefundId;   // ref_<uuid> (latest / full refund)
    private String providerVoidId;

    private Instant createdAt;

    private Instant updatedAt;

}
