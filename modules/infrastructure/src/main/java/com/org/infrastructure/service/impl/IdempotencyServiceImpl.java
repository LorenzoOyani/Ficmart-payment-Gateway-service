package com.org.infrastructure.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.application.dto.AuthorizeResponse;
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
    public IdempotencyState<AuthorizeResponse> begin(IdempotencyCommand command, Class<AuthorizeResponse> clazzType) throws IdempotencyIdentityConflictException {
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

            return transactionTemplate.execute(status ->{
                Optional<IdempotencyEntity> existingRec = idempotencyRepository.findByIdempotencyKeyAndOperation(
                        command.merchantId(),
                        command.requestHash(),
                        command.Operation()
                );

                if (existingRec.isPresent()) {
                    var record = existingRec.get();

                    var recordDto = idempotencyMapper.IdempotencyRecordMapper(record);

                    validateRequestHash(recordDto, command);

                    if (recordDto.status() == IdempotencyStatus.COMPLETED ||
                            recordDto.status() == IdempotencyStatus.FAILED) {
                        T response = deserialize(recordDto.responseBody(), (Class<T>) clazzType);

                        return IdempotencyState.replay((PaymentResponse) response);

                    }
                    if (isStale(recordDto)){
                        recordDto.markedFailed(
                                409,
                                "{\"error\":\"Stale in-progress idempotency record detected\"}"

                        );
                        var entity = idempotencyMapper.IdempotencyEntityMapper(recordDto);
                        idempotencyRepository.save(entity);

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

                var entity = idempotencyMapper.IdempotencyEntityMapper(record);

                try{
                    idempotencyRepository.save(entity);
                }catch (DataIntegrityViolationException ex){
                    return IdempotencyState.inProgress();
                }

                return IdempotencyState.acquired();
            });
        } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("problem acquiring lock key");
        }finally {
            if (locked && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    private void validateRequestHash(IdempotencyRecord record, IdempotencyCommand command) {
        if (!Objects.equals(record.requestHash(), command.requestHash())) {
            throw new IllegalArgumentException("request hash does not match");
        }
    }


    @Override
    public void complete(IdempotencyCommand command, AuthorizeResponse response) {
            transactionTemplate.executeWithoutResult(result -> {
                Optional<IdempotencyEntity> idempotencyEntity = Optional.ofNullable(idempotencyRepository.findByIdempotencyKeyAndOperation(
                        command.merchantId(),
                        command.requestHash(),
                        command.key()
                ).orElseThrow(() -> new DataIntegrityViolationException("IDEMPOTENCY RECORDS NOT FOUND")));


                    if(idempotencyEntity.isPresent()) {
                        IdempotencyEntity recordEntity = idempotencyEntity.get();
                        IdempotencyRecord r = idempotencyMapper.IdempotencyRecordMapper(recordEntity);

                        validateRequestHash(r, command);
                        r.markCompleted(200, serialize((T) response));

                        idempotencyRepository.save(recordEntity);
                    }
            });

    }

    @Override
    public void fail(IdempotencyCommand command, AuthorizeResponse response) {
        transactionTemplate.executeWithoutResult(result ->{
            Optional<IdempotencyEntity> idempotencyEntity = Optional.ofNullable(idempotencyRepository.findByIdempotencyKeyAndOperation(
                    command.merchantId(),
                    command.requestHash(),
                    command.key()
            ).orElseThrow(() -> new DataIntegrityViolationException("IDEMPOTENCY RECORDS NOT FOUND")));

            if(idempotencyEntity.isPresent()) {
                IdempotencyEntity recordEntity = idempotencyEntity.get();
                IdempotencyRecord r = idempotencyMapper.IdempotencyRecordMapper(recordEntity);




                r.markedFailed(200, serialize((T) response));
                idempotencyRepository.save(recordEntity);
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
