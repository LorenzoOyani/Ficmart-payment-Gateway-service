package com.org.domain.model;


import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;


@Getter
@Setter
public class Account {

    private Long id;

    private String accountNumber;

    private String cvv;

    private int expiryMonth;

    private long balanceCents;

    private Timestamp createdAt;

    private Timestamp updatedAt;


}
