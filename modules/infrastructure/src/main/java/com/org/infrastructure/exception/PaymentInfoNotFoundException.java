package com.org.infrastructure.exception;

public class PaymentInfoNotFoundException extends RuntimeException {

    public PaymentInfoNotFoundException(String message) {
        super(message);
    }
}
