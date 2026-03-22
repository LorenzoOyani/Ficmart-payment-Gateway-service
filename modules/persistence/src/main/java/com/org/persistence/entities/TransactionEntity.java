package com.org.persistence.entities;

import com.org.domain.dto.BankAuthorizationCmd;
import jakarta.persistence.*;
import java.time.Instant;
import com.org.domain.enums.TransactionStatus;
import com.org.domain.enums.TransactionType;
import lombok.Getter;

@Getter
@Entity
@Table(name = "transactions"

)
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transactioonId", nullable = false, unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "reference_id", columnDefinition = "uuid")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    public TransactionEntity capture(String transactionId, BankAuthorizationCmd cmd) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.amountCents = cmd.amount();
        transaction.referenceId = this.referenceId;
        transaction.transactionId = transactionId;
        transaction.type = TransactionType.CAPTURE;


        return transaction;


    }

}
