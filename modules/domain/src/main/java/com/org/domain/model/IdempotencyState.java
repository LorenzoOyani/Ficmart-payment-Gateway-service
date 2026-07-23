package com.org.domain.model;

import lombok.Getter;

@Getter
public class IdempotencyState {
    private final boolean replay;
    private final boolean inProgress;
   private final Object paymentResponse;

    public IdempotencyState(boolean replay, boolean inProgress, Object paymentResponse) {
        this.replay = replay;
        this.inProgress = inProgress;
        this.paymentResponse = paymentResponse;
    }


    public static IdempotencyState replay(Object paymentResponse) {
        return new IdempotencyState(true, false, paymentResponse);
    }

    public static IdempotencyState inProgress() {
        return new IdempotencyState(true, true, null);
    }

    public IdempotencyState complete() {
        return new IdempotencyState(false, true, paymentResponse);
    }

    public static IdempotencyState acquired() {
        return new IdempotencyState(false, false, null);
    }
}

