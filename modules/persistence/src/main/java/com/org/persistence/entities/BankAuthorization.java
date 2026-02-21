package com.org.persistence.entities;

import com.org.domain.dto.BankAuthorizationCmd;
import com.org.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter

@Entity
@Table(name = "bankAuthorization")
public class BankAuthorization  {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "auth_id", nullable = false, length = 64, unique = true)
    private String authId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "Paymentstatus", nullable = false, length = 20)
    private PaymentStatus status;


    public BankAuthorization authorize(String authId, BankAuthorizationCmd cmd) {
        BankAuthorization authorization = new BankAuthorization();
        authorization.id = UUID.randomUUID();
        authorization.authId = authId;
        authorization.orderId = cmd.orderId();
        authorization.customerId = cmd.customerId();
        authorization.amountCents = cmd.amount();
        authorization.status = PaymentStatus.AUTHORIZED;

        return authorization;
    }


    public BankAuthorization capture(String authId, BankAuthorizationCmd cmd) {
        BankAuthorization authorization = new BankAuthorization();
        authorization.id = UUID.randomUUID();
        authorization.authId = authId;
        authorization.orderId = cmd.orderId();
        authorization.customerId = cmd.customerId();
        authorization.amountCents = cmd.amount();
        authorization.status = PaymentStatus.CAPTURED;

        return authorization;
    }

    public PaymentStatus status(){
        if(this.status != PaymentStatus.AUTHORIZED){
            throw new IllegalStateException("BankAuthorization status is not AUTHORIZED");
        }
        return this.status;
    }

    public void markCaptured() {
        if(status != PaymentStatus.AUTHORIZED) throw new IllegalStateException("BankAuthorization status is not AUTHORIZED");
        this.status = PaymentStatus.CAPTURED;
    }
}
