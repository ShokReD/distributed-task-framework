package com.distributed_task_framework.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be empty string");
        }
        return value;
    }
}
