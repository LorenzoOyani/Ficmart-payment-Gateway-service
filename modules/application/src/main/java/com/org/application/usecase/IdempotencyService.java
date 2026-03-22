package com.org.application.usecase;

import com.org.application.ports.IdempotencyPorts;
import com.org.persistence.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IdempotencyService implements IdempotencyPorts {


    private final IdempotencyRepository idempotencyRepository;



}
