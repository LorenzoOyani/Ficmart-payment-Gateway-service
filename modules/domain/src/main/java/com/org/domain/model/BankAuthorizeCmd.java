package com.org.domain.model;

public record BankAuthorizeCmd(String orderId, String customerId, long amount) {
}
