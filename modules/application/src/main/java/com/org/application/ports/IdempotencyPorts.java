package com.org.application.ports;

import com.org.domain.dto.IdemKeyResponse;

import java.util.Optional;

public interface IdempotencyPorts {


    Optional<IdemKeyResponse> findIdemKey(String idemKey, String endpoint);
}
