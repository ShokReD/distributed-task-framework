package com.distributed_task_framework.model;

import java.util.UUID;

public record TaskNameAndNode(
    String taskName,
    UUID nodeId
) {
}