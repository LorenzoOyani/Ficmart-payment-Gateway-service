package com.org.infrastructure.service;

import com.org.application.dto.AuthorizeResponse;
import com.org.domain.dto.IdempotencyCommand;
import com.org.domain.exception.IdempotencyIdentityConflictException;
import com.org.domain.model.IdempotencyState;

public interface IdempotencyService<T> {
     IdempotencyState<AuthorizeResponse> begin(IdempotencyCommand command, Class<AuthorizeResponse> classType) throws IdempotencyIdentityConflictException;

     void complete(IdempotencyCommand command, AuthorizeResponse response);

     void fail(IdempotencyCommand command, AuthorizeResponse response);

}
