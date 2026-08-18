package com.org.infrastructure.service;

import com.org.domain.dto.IdempotencyCommand;
import com.org.domain.exception.IdempotencyIdentityConflictException;
import com.org.domain.model.IdempotencyState;

public interface IdempotencyService {

     IdempotencyState begin(IdempotencyCommand command)
             throws IdempotencyIdentityConflictException;

     <T> void complete(IdempotencyCommand command, T response);

    <T> void fail(IdempotencyCommand command, T response);


}