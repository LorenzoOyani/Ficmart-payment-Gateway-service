package com.org.persistence.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "account_number")
    private String accountNumber;

    @Column(length = 3, nullable = false)
    private String cvv;

    @Column(name= "expiry_month",nullable = false)
    private int expiryMonth;

    @Column(name = "balance_cents",nullable = false)
    private long balanceCents;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}
