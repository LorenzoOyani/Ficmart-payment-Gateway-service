package com.org.domain.model;


import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
@Getter
@Setter
public class Account {

    private Long id;

    private String accountNumber;

    private long balanceCents;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    public void debit(long amount) {
        if (amount <= 0) {
            throw new IllegalStateException("Amount must be positive");

        }
        if(balanceCents > amount) {

            balanceCents -= amount;
        }

    }

    public void credit(long amount) {
        if (amount <= 0) {
            throw new IllegalStateException("Amount must be positive");
        }
        balanceCents += amount;
    }

}
