package com.org.domain.dto;

import java.util.function.Supplier;

public interface IdempotencyContract {
    <T> T execute(IdempotencyCommand command, Supplier<T> supplier, Class<T> clazzType);


}
