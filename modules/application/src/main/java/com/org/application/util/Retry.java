package com.org.application.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

public final class Retry {

    private static final Logger log = LoggerFactory.getLogger(Retry.class);

    private Retry() {}

    public static <T> T retries(int maxAttempts, Duration backoff, Supplier<T> fn) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return fn.get();
            } catch (Exception e) {
                boolean isTransient = transients(e);

                if (!isTransient || attempt >= maxAttempts) {
                    throw e;
                }

                Duration sleepFor = jitterBackOff(backoff, attempt);
                log.warn("Transient failure (attempt {}/{}). Sleeping {}ms. Cause: {}",
                        attempt, maxAttempts, sleepFor.toMillis(), safeMsg(e));
                sleep(sleepFor);
            }
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("RETRY_INTERRUPTED", ex);
        }
    }

    private static Duration jitterBackOff(Duration backoff, int attempt) {
        long ms = backoff.toMillis() * attempt;
        long jitter = (long) (Math.random() * 50);
        return Duration.ofMillis(ms + jitter);
    }

    private static boolean transients(Exception e) {
        String message = e.getMessage() == null ? "" : e.getMessage();
        // Keep this simple, but consistent with your provider mapping
        return message.contains("BANK_500")
                || message.contains("500")
                || message.contains("TIMEOUT")
                || message.contains("I/O");
    }

    private static String safeMsg(Exception e) {
        String m = e.getMessage();
        return (m == null || m.isBlank()) ? e.getClass().getSimpleName() : m;
    }
}
