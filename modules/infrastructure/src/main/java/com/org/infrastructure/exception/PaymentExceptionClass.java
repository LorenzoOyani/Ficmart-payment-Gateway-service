package com.org.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PaymentExceptionClass {

    @ExceptionHandler(PaymentInfoNotFoundException.class)
    public ResponseEntity<?> handlePaymentInfoNotFoundException(PaymentInfoNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError("PAYMENT_NOT_FOUND", e.getMessage()));

    }

    private record ApiError(String paymentNotFound, String message) {}
}
