package com.org.infrastructure.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.application.dto.AuthorizeResponse;
import com.org.application.dto.CancelResponse;
import com.org.domain.dto.IdempotencyCommand;
import com.org.domain.enums.IdempotencyStatus;
import com.org.domain.exception.IdempotencyIdentityConflictException;
import com.org.domain.model.IdempotencyRecord;
import com.org.domain.model.IdempotencyState;
import com.org.infrastructure.service.IdempotencyService;
import com.org.persistence.entities.IdempotencyEntity;
import com.org.persistence.mapper.IdempotencyMapper;
import com.org.persistence.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final Duration LOCK_WAIT = Duration.ofSeconds(3);
    private static final Duration LOCK_LEASE = Duration.ofSeconds(10);
    private static final Duration STALE_IN_PROGRESS_AFTER = Duration.ofMinutes(5);
    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotencyServiceImpl.class);

    private final IdempotencyRepository idempotencyRepository;
    private final IdempotencyMapper idempotencyMapper;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private final RedissonClient redissonClient;

    @Override
    public IdempotencyState begin(IdempotencyCommand command)
            throws IdempotencyIdentityConflictException {

        String lockKey = buildLockKey(command);
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            locked = lock.tryLock(
                    LOCK_WAIT.toSeconds(),
                    LOCK_LEASE.toSeconds(),
                    TimeUnit.SECONDS
            );

            if (!locked) {
                return IdempotencyState.inProgress();
            }

            return transactionTemplate.execute(status -> {
                Optional<IdempotencyEntity> existingRec =
                        idempotencyRepository.findByIdempotencyKeyAndOperation(
                                command.merchantId(),
                                command.key(),
                                command.Operation()
                        );

                if (existingRec.isPresent()) {
                    IdempotencyEntity entity = existingRec.get();
                    IdempotencyRecord record = idempotencyMapper.IdempotencyRecordMapper(entity);

                    validateRequestHash(record, command);

                    if (record.status() == IdempotencyStatus.COMPLETED ||
                            record.status() == IdempotencyStatus.FAILED) {

                        AuthorizeResponse response = deserialize(record.responseBody());
                        return IdempotencyState.replay(response);
                    }

                    if (isStale(record)) {
                        record.markedFailed(
                                409,
                                "{\"error\":\"Stale idempotency record detected\"}"
                        );

                        idempotencyRepository.save(
                                idempotencyMapper.IdempotencyEntityMapper(record)
                        );

                        return IdempotencyState.inProgress();
                    }

                    return IdempotencyState.inProgress();
                }

                IdempotencyRecord record = IdempotencyRecord.inProgress(
                        command.merchantId(),
                        command.key(),
                        command.requestHash(),
                        command.Operation()
                );

                IdempotencyEntity entity = idempotencyMapper.IdempotencyEntityMapper(record);

                try {
                    idempotencyRepository.save(entity);
                } catch (DataIntegrityViolationException ex) {
                    return IdempotencyState.inProgress();
                }

                return IdempotencyState.acquired();
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("problem acquiring lock key", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public <T> void complete(IdempotencyCommand command, T response) {
        transactionTemplate.executeWithoutResult(result -> {
            IdempotencyEntity entity = idempotencyRepository.findByIdempotencyKeyAndOperation(
                    command.merchantId(),
                    command.key(),
                    command.Operation()
            ).orElseThrow(() -> new DataIntegrityViolationException("IDEMPOTENCY RECORD NOT FOUND"));

            IdempotencyRecord record = idempotencyMapper.IdempotencyRecordMapper(entity);

            validateRequestHash(record, command);

            record.markCompleted(200, serialize((CancelResponse)response));

            idempotencyRepository.save(
                    idempotencyMapper.IdempotencyEntityMapper(record)
            );
        });
    }

    @Override
    public <T> void fail(IdempotencyCommand command, T response) {
                transactionTemplate.executeWithoutResult(result -> {
            IdempotencyEntity entity = idempotencyRepository.findByIdempotencyKeyAndOperation(
                    command.merchantId(),
                    command.key(),
                    command.Operation()
            ).orElseThrow(() -> new DataIntegrityViolationException("IDEMPOTENCY RECORD NOT FOUND"));

            IdempotencyRecord record = idempotencyMapper.IdempotencyRecordMapper(entity);

            validateRequestHash(record, command);

            record.markedFailed(500, serialize((AuthorizeResponse) response));

            idempotencyRepository.save(
                    idempotencyMapper.IdempotencyEntityMapper(record)
            );
        });

    }

    private void validateRequestHash(IdempotencyRecord record, IdempotencyCommand command) {
        if (!Objects.equals(record.requestHash(), command.requestHash())) {
            throw new IllegalArgumentException("request hash does not match");
        }
    }

    private String buildLockKey(IdempotencyCommand command) {
        return "idem:lock:%s:%s:%s".formatted(
                command.merchantId(),
                command.key(),
                command.Operation()
        );
    }

    private boolean isStale(IdempotencyRecord record) {
        return record.status() == IdempotencyStatus.IN_PROGRESS
                && record.lockedAt() != null
                && record.lockedAt().isBefore(Instant.now().minus(STALE_IN_PROGRESS_AFTER));
    }

    private String serialize(AuthorizeResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            LOGGER.error("failed to serialize response", e);
            throw new IllegalStateException("failed to serialize response", e);
        }
    }

    private String serialize(CancelResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            LOGGER.error("failed to serialize response", e);
            throw new IllegalStateException("failed to serialize response", e);
        }
    }

    private AuthorizeResponse deserialize(String responseBodyJson) {
        try {
            return objectMapper.readValue(responseBodyJson, AuthorizeResponse.class);
        } catch (Exception e) {
            LOGGER.error("failed to deserialize response", e);
            throw new IllegalStateException("failed to deserialize response", e);
        }
    }
}
