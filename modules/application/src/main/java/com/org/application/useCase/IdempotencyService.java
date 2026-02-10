package com.org.application.useCase;

import com.org.domain.dto.IdemKeyResponse;
import com.org.application.ports.IdempotencyPorts;
import com.org.persistence.entities.IdempotencyEntity;
import com.org.persistence.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class IdempotencyService implements IdempotencyPorts {


    private final IdempotencyRepository idempotencyRepository;


    @Override
    public Optional<IdemKeyResponse> findIdemKey(String idemKey, String endpoint) {
        Optional<IdempotencyEntity> idemKeys = idempotencyRepository.findByIdempotencyKeyAndEndpoint(idemKey, endpoint);
        if (idemKeys.isEmpty()) {
            return Optional.empty();
        }
        return idemKeys.map(e -> new IdemKeyResponse(
                e.getIdempotencyKey(),
                e.getEndpoint(),
                e.getRequestHash(),
                e.getIdempotencyStatus(),
                e.getResponseBodyJson()

        ));
    }
}
