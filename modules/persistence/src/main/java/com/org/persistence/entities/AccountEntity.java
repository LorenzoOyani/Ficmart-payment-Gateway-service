package com.org.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "account_number")
    private String accountNumber;

    @Column(name = "customerId", nullable = false)
    private String customerId;

    @Column(name = "balance_cents",nullable = false)
    private long balanceCents;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    public void debit(long amount) {
       if(amount <= 0) throw new IllegalArgumentException("amount must be greater than zero");
       if(balanceCents < amount) throw new IllegalArgumentException("amount must be lower than balance cents");

       balanceCents -= amount;
    }

    public void credit(long amount) {
        if(amount <= 0) throw new IllegalArgumentException("amount must be greater than zero");
        balanceCents += amount;
    }
}
