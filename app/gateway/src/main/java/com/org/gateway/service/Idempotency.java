package com.org.gateway.service;

import com.org.domain.dto.IdempotencyCommand;
import com.org.domain.dto.IdempotencyService;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class Idempotency implements IdempotencyService {

    @Override
    public <T> T execute(IdempotencyCommand command, Supplier<T> supplier, Class<T> clazzType) {
        return null;
    }
}
