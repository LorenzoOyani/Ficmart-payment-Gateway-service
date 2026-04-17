package com.org.infrastructure.stripe;

public class StripeGatewayException extends RuntimeException {
    public StripeGatewayException(String refundFailure) {
    }
}
