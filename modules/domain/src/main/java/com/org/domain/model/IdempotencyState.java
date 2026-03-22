package com.org.domain.model;

import com.org.domain.dto.PaymentResponse;
import lombok.Getter;

@Getter
public class IdempotencyState<T> {
    private final boolean replay;
    private final boolean inProgress;
   private final T paymentResponse;

    public IdempotencyState(boolean replay, boolean inProgress, T paymentResponse) {
        this.replay = replay;
        this.inProgress = inProgress;
        this.paymentResponse = paymentResponse;
    }


    public static IdempotencyState replay(PaymentResponse paymentResponse) {
        return new IdempotencyState<>(true, false, paymentResponse);
    }

    public static IdempotencyState inProgress() {
        return new IdempotencyState(true, true, null);
    }

    public IdempotencyState<T> complete() {
        return new IdempotencyState(false, true, paymentResponse);
    }

    public static IdempotencyState acquired() {
        return new IdempotencyState(false, false, null);
    }
}

