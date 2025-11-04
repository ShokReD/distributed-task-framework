package com.distributed_task_framework.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public String defaultIfBlank(String checkedValue, String defaultValue) {
        return isBlank(checkedValue) ? defaultValue : checkedValue;
    }

    public String requireNotBlank(String value, String name) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(name + " must not be empty string");
        }
        return value;
    }
}
