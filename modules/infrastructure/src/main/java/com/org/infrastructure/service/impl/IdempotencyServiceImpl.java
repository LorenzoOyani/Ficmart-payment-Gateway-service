package com.org.infrastructure.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.domain.dto.IdempotencyCommand;
import com.org.domain.dto.PaymentResponse;
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
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class IdempotencyServiceImpl<T> implements IdempotencyService<T> {
    private static final Duration LOCK_WAIT = Duration.ofSeconds(3);
    private static final Duration LOCK_LEASE = Duration.ofSeconds(10);
    private static final Duration STALE_IN_PROGRESS_AFTER = Duration.ofMinutes(5);
    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotencyServiceImpl.class);


    private final IdempotencyRepository idempotencyRepository;

    private final IdempotencyMapper idempotencyMapper;

    private final ObjectMapper objectMapper;

    private final TransactionTemplate transactionTemplate;

    private final RedissonClient redissonClient;


    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @SuppressWarnings("unchecked assignment")
    @Override
    public IdempotencyState<T> begin(IdempotencyCommand command, Class<T> clazzType) throws IdempotencyIdentityConflictException {
        String lockKey = buildLockKey(command);
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            locked = lock.tryLock(
                    LOCK_WAIT.toSeconds(),
                    LOCK_LEASE.toSeconds(),
                    TimeUnit.MINUTES

            );

            if (!locked) {
                return IdempotencyState.inProgress();
            }

            return transactionTemplate.execute(result -> {


                var existingRecord = idempotencyRepository.findByIdempotencyKeyAndOperation(
                        command.merchantId(),
                        command.Operation(),
                        command.requestHash()
                );

                IdempotencyEntity record;
                IdempotencyRecord r = null;

                if (existingRecord.isPresent()) {
                    record = existingRecord.get();
                    r = idempotencyMapper.IdempotencyRecordMapper(record);
                    if (!Objects.equals(r.requestHash(), command.requestHash())) {
                        LOGGER.error("");
                    }

                    if (r.status() == IdempotencyStatus.COMPLETED || record.getIdempotencyStatus() == IdempotencyStatus.FAILED) {
                        T replayResponse = deserialize(record.getResponseBodyJson(), clazzType);
                        return IdempotencyState.replay((PaymentResponse) replayResponse);

                    }

                    if (isStale(r)) {
                        r.markedFailed(
                                500, "staled data 'in-progress' detected , replaying response"
                        );
                        idempotencyRepository.save(record); /// save the failed in-progress record to DB for future consistency
                        return IdempotencyState.inProgress();
                    }

                    return IdempotencyState.inProgress();
                }

                if (r != null) {

                    var created = r.inProgress(
                            command.merchantId(),
                            command.key(),
                            command.requestHash(),
                            command.Operation()

                    );

                    record = idempotencyMapper.IdempotencyEntityMapper(created);

                    try {
                        idempotencyRepository.save(record);
                    } catch (DataIntegrityViolationException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
                return IdempotencyState.inProgress();
            });
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException("");
        }finally {
            if(locked && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }


    }

    @Override
    public void complete(IdempotencyCommand command, T response) {
            transactionTemplate.executeWithoutResult(result -> {
                Optional<IdempotencyEntity> idempotencyEntity = Optional.ofNullable(idempotencyRepository.findByIdempotencyKeyAndOperation(
                        command.merchantId(),
                        command.requestHash(),
                        command.key()
                ).orElseThrow(() -> new DataIntegrityViolationException("IDEMPOTENCY RECORDS NOT FOUND")));

                if(idempotencyEntity.isPresent()) {
                    IdempotencyEntity recordEntity = idempotencyEntity.get();
                    IdempotencyRecord r = idempotencyMapper.IdempotencyRecordMapper(recordEntity);


                    r.markCompleted(200, serialize(response));
                }

            });

    }

    @Override
    public void fail(IdempotencyCommand command, T response) {
        transactionTemplate.executeWithoutResult(result ->{
            Optional<IdempotencyEntity> idempotencyEntity = Optional.ofNullable(idempotencyRepository.findByIdempotencyKeyAndOperation(
                    command.merchantId(),
                    command.requestHash(),
                    command.key()
            ).orElseThrow(() -> new DataIntegrityViolationException("IDEMPOTENCY RECORDS NOT FOUND")));

            if(idempotencyEntity.isPresent()) {
                IdempotencyEntity recordEntity = idempotencyEntity.get();
                IdempotencyRecord r = idempotencyMapper.IdempotencyRecordMapper(recordEntity);


                r.markedFailed(200, serialize(response));
            }

        });
    }

    private String buildLockKey(IdempotencyCommand command) {
        return "idem:lock:%s:%s:%s".formatted(
                command.merchantId(),
                command.requestHash(),
                command.key()
        );
    }

    private boolean isStale(IdempotencyRecord r) {
        return r.status() == IdempotencyStatus.IN_PROGRESS
                && r.lockedAt() != null
                && r.lockedAt().isBefore(Instant.now().minus(STALE_IN_PROGRESS_AFTER));

    }

    private String serialize(T response) {
        try{
            return objectMapper.writeValueAsString(response);


        }catch (JsonProcessingException e){
            LOGGER.error(e.getMessage());
            throw new IllegalStateException("failed to serialize response");
        }
    }

    private T deserialize(String responseBodyJson, Class<T> clazzType) {
        try {
            return objectMapper.readValue(responseBodyJson, clazzType);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new IllegalStateException("failed to deserialize response");
        }
    }
}
