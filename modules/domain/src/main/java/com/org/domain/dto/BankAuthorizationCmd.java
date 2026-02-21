package com.org.domain.dto;

import com.org.domain.enums.PaymentStatus;
import com.org.domain.model.BankAuthorizeCmd;

import java.util.UUID;

public record BankAuthorizationCmd(

        UUID id,
        String orderId,
        String customerId,
        String authId,
        long amount,
        PaymentStatus status

) {
}
