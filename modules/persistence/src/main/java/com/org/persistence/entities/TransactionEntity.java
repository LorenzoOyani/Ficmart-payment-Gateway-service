package com.org.persistence.entities;

import jakarta.persistence.*;

import java.time.Instant;
import com.org.domain.enums.TransactionStatus;
import com.org.domain.enums.TransactionType;

@Entity
@Table(name = "transactions"

)
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private AccountEntity account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,  length = 20)
    private TransactionType type;

    @Column(name="amount_cents", nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name="reference_id", columnDefinition = "uuid")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name="expires_at")
    private Instant expiresAt;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;


}
