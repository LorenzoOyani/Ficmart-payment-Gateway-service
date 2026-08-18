package com.org.infrastructure.stripe;

import com.stripe.exception.StripeException;
import lombok.Getter;

@Getter
public class StripeGatewayException extends RuntimeException {

    private final String code;
    private final Integer statusCode;
    private final String requestId;

    public StripeGatewayException(String message, String code, Integer statusCode, String requestId, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.statusCode = statusCode;
        this.requestId = requestId;
    }



    public static StripeGatewayException from(StripeException ex) {
        return new StripeGatewayException(
                ex.getMessage(),
                ex.getCode(),
                ex.getStatusCode(),
                ex.getRequestId(),
                ex
        );
    }




}
