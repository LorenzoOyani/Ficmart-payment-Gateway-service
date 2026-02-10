package com.org.domain.model;



import com.org.domain.enums.TransactionStatus;
import com.org.domain.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
public class Transaction {

    private long id;

    private Account account;

    private TransactionType type;

    private long amountCents;

    private String currency = "USD";

    private Long referenceId;

    private TransactionStatus status;

    private Instant expiresAt;

    private String metadata;

    private Instant createdAt;
}
