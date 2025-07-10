package com.distributed_task_framework.settings;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public final class FixedRetry implements Retry {

    /**
     * Delay between retires.
     */
    private final Duration delay;
    /**
     * Max attempts
     */
    private final Integer maxNumber;
    /**
     * Max interval for retires.
     * Give up after whether max attempts is reached or interval is passed.
     */
    private final Duration maxInterval;

    public FixedRetry(Duration delay, Integer maxNumber, Duration maxInterval) {
        this.delay = delay;
        this.maxNumber = maxNumber;
        this.maxInterval = maxInterval;
    }

    @Override
    public Optional<LocalDateTime> next(int currentFails, Clock clock) {
        if (currentFails >= maxNumber) {
            return Optional.empty();
        }
        Duration passedTime = Duration.ofSeconds(currentFails * delay.getSeconds());
        if (maxInterval != null && passedTime.compareTo(maxInterval) >= 0) {
            return Optional.empty();
        }
        return Optional.of(LocalDateTime.now(clock).plus(delay));
    }
}
