package com.org.domain.model;


import com.org.domain.dto.BankAuthorizationCmd;
import com.org.domain.enums.TransactionStatus;
import com.org.domain.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
public class Transaction {

    private UUID id;

    private String authId;

    private String transactionId;

    private TransactionType type;

    private long amountCents;

    private String currency = "USD";

    private Long referenceId;

    private String orderId;

    private String customerId;


    private TransactionStatus status;

    private Instant expiresAt;

    private String metadata;

    private Instant createdAt;


    public Transaction capture(String authId, BankAuthorizationCmd cmd) {
        Transaction transaction = new Transaction();
        transaction.id = UUID.randomUUID();
        transaction.authId = authId;
        transaction.orderId = cmd.orderId();
        transaction.customerId = cmd.customerId();
        transaction.amountCents = cmd.amount();
        transaction.type = TransactionType.CAPTURE;

        return transaction;
    }

    public Transaction refund(String authId, BankAuthorizationCmd cmd) {
        Transaction transaction = new Transaction();
        transaction.id = UUID.randomUUID();
        transaction.authId = authId;
        transaction.orderId = cmd.orderId();
        transaction.customerId = cmd.customerId();
        transaction.amountCents = cmd.amount();
        transaction.type = TransactionType.REFUND;
        return transaction;
    }
}
