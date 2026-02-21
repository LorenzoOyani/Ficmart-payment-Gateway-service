package com.org.domain.dto;

public record BankAuthorizeResult(boolean authorized, String authId, String tranId) {
}
