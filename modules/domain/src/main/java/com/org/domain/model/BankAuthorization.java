package com.org.domain.model;

import com.org.domain.dto.BankAuthorizationCmd;
import com.org.domain.enums.PaymentStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BankAuthorization {
    private UUID id;

    private String authId;

    private PaymentStatus state;

    private String orderId;

    private String customerId;

    private long amountCents;


    public static BankAuthorization markedAuthorize(String authId, String orderId, String customerId, long amountCents) {
        BankAuthorization bankAuthorization = new BankAuthorization();
        bankAuthorization.authId = authId;
        bankAuthorization.orderId = orderId;
        bankAuthorization.state = PaymentStatus.AUTHORIZED;
        bankAuthorization.customerId = customerId;
        bankAuthorization.amountCents = amountCents;

        return bankAuthorization;
    }

    public static BankAuthorization markedCaptured(String authId, BankAuthorizationCmd cmd) {
        BankAuthorization bankAuthorization = new BankAuthorization();
        bankAuthorization.authId = authId;
        bankAuthorization.orderId = cmd.orderId();
        bankAuthorization.state = PaymentStatus.CAPTURED;
        bankAuthorization.customerId = cmd.customerId();
        bankAuthorization.amountCents = cmd.amount();


        return bankAuthorization;
    }


}
