package com.distributed_task_framework.settings;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

public final class OffRetry implements Retry {
    @Override
    public Optional<LocalDateTime> next(int currentFails, Clock clock) {
        return Optional.empty();
    }
}
