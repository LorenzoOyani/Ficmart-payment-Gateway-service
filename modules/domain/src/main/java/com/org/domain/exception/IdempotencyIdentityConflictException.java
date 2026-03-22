package com.org.domain.exception;

public class IdempotencyIdentityConflictException extends RuntimeException {
    public IdempotencyIdentityConflictException(String s) {
        super(s);
    }
}
