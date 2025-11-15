package com.distributed_task_framework.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Exponential backoff retry policy.
 * <pre>nextRetry = failCount == 1 ? currentTime + initialDelay : currentTime + delayPeriod*2^(failCount-1)</pre>
 * where <pre>failCount = 1, 2, 3, ... nextRetry = Min(nextRetry, currentTime + maxDelay)</pre>
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class BackoffRetry implements Retry {
    public static final BackoffRetry DEFAULT = BackoffRetry.builder().build();

    /**
     * Initial delay of the first retry.
     */
    @Builder.Default
    Duration initialDelay = Duration.ofSeconds(10);
    /**
     * The time interval that is the ratio of the exponential backoff formula (geometric progression)
     */
    @Builder.Default
    Duration delayPeriod = Duration.ofSeconds(10);
    /**
     * Maximum number of times a tuple is retried before being acked and scheduled for commit.
     */
    @Builder.Default
    int maxRetries = 32;
    /**
     * Maximum amount of time waiting before retrying.
     */
    @Builder.Default
    Duration maxDelay = Duration.ofHours(1);

    // nextRetry = failCount == 1 ? currentTime + initialDelay : currentTime + delayPeriod*2^(failCount-1)
    @Override
    public Optional<LocalDateTime> next(int currentFails, Clock clock) {
        if (currentFails == 0 || currentFails > maxRetries) {
            return Optional.empty();
        }
        long nowMillis = clock.millis();
        long nextRetryMillis = currentFails == 1 ?
            nowMillis + initialDelay.toMillis() :
            nowMillis + (long) (delayPeriod.toMillis() * Math.pow(2, currentFails - 1));

        nextRetryMillis = Math.min(Math.abs(nextRetryMillis), nowMillis + maxDelay.toMillis());
        return Optional.of(Instant.ofEpochMilli(nextRetryMillis).atZone(clock.getZone()).toLocalDateTime());
    }
}
