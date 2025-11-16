package com.distributed_task_framework.model;

import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class BatchRouteMap {
    @Builder.Default
    Map<Partition, Integer> partitionLimits = Map.of();
    @Builder.Default
    Map<TaskNameAndNode, Integer> taskNameNodeQuota = new HashMap<>();
}
