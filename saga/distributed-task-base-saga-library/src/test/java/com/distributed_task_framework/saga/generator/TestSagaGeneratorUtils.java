package com.distributed_task_framework.saga.generator;

import com.distributed_task_framework.saga.settings.SagaMethodSettings;
import com.distributed_task_framework.saga.settings.SagaSettings;
import com.distributed_task_framework.settings.Retry;
import com.distributed_task_framework.settings.RetryMode;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;

import java.time.Duration;

@UtilityClass
public class TestSagaGeneratorUtils {

    public SagaMethodSettings withoutRetry() {
        return SagaMethodSettings.DEFAULT.toBuilder()
            .retry(Retry.builder()
                .retryMode(RetryMode.OFF)
                .build()
            )
            .build();
    }

    @SafeVarargs
    public SagaMethodSettings withNoRetryFor(Class<? extends Throwable>... exceptions) {
        return SagaMethodSettings.DEFAULT.toBuilder()
            .noRetryFor(Lists.newArrayList(exceptions))
            .build();
    }

    public static SagaSettings withAvailableAfterCompletionTimeout(Duration duration) {
        return SagaSettings.DEFAULT.toBuilder()
            .availableAfterCompletionTimeout(duration)
            .build();
    }

    public static SagaSettings withExpirationTimeout(Duration duration) {
        return SagaSettings.DEFAULT.toBuilder()
            .expirationTimeout(duration)
            .build();
    }
}
