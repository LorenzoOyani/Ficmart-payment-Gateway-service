package com.org.infrastructure.service;

import com.org.domain.dto.IdempotencyCommand;
import com.org.domain.exception.IdempotencyIdentityConflictException;
import com.org.domain.model.IdempotencyState;

public interface IdempotencyService<T> {
     IdempotencyState<T> begin(IdempotencyCommand command, Class<T> classType) throws IdempotencyIdentityConflictException;

     void complete(IdempotencyCommand command, T response);

     void fail(IdempotencyCommand command, T response);

}
