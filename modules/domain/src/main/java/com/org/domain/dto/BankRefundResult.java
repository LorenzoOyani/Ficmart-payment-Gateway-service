package com.org.domain.dto;

public record BankRefundResult(boolean success, String tnxId, String reason) {
}
