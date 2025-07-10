package com.distributed_task_framework.settings;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

public sealed interface Retry permits OffRetry, FixedRetry, BackoffRetry {
    Optional<LocalDateTime> next(int currentFails, Clock clock);
}
